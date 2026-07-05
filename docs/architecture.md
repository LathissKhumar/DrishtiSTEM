# Architecture

> Authoritative architecture specification for the Drishti SDK. All AI coding agents, contributors, and maintainers MUST treat this document as the architectural contract.

## Vision

Drishti SDK is an open-source, modular Spatial Intelligence & Accessibility SDK that transforms visual content into accessible multimodal experiences.

The SDK is **application-independent**. Applications (DrishtiSTEM, navigation apps, museum guides, robotics platforms, AR systems) consume the SDK through its public APIs. Applications MUST NOT directly interact with internal modules.

---

## Architecture Principles

| Principle | Meaning |
|:---|:---|
| **Modular** | Each capability is a separate module published independently |
| **Plugin-first** | Every content type is a plugin; core knows nothing about specific domains |
| **AI-model agnostic** | No direct model references in business logic; always through provider interfaces |
| **Hardware agnostic** | Platform-specific code stays inside adapters |
| **Event-driven** | Modules communicate through events, not direct calls |
| **Cross-platform** | KMP commonMain/androidMain; platform code in expect/actual |
| **On-device first** | No cloud calls. All ML runs on-device |
| **Extensible** | New content types require zero core changes |
| **Testable** | Every module has unit, integration, and regression tests |
| **Dependency inversion** | High-level modules never import low-level modules |

---

## Layered Architecture

```
┌─────────────────────────────────────────────┐
│  Layer 1: Application Layer                 │
│  DrishtiSTEM, accessibility apps, third-    │
│  party apps                                 │
├─────────────────────────────────────────────┤
│  Layer 2: SDK Public API                    │
│  Drishti, DrishtiDiagram, Frame, ContentItem│
├─────────────────────────────────────────────┤
│  Layer 3: Runtime Layer                     │
│  Pipeline, PluginRegistry, ExplorationSession│
├─────────────────────────────────────────────┤
│  Layer 4: Plugin Layer                      │
│  DetectorPlugin, RendererPlugin,            │
│  HapticsRenderer, AudioRenderer,            │
│  VoiceOutputRenderer                        │
├─────────────────────────────────────────────┤
│  Layer 5: Core AI Modules                   │
│  Graph, Formula, Molecule, Vision, Voice    │
├─────────────────────────────────────────────┤
│  Layer 6: Model Providers                   │
│  OpenCV, OpenChemLib, mXparser, Sherpa-ONNX │
├─────────────────────────────────────────────┤
│  Layer 7: Platform Abstraction              │
│  expect/actual: commonMain ↔ androidMain    │
├─────────────────────────────────────────────┤
│  Layer 8: Hardware                          │
│  Camera, GPU, NPU, Touch, Speaker,          │
│  Microphone, Haptic Motor                   │
└─────────────────────────────────────────────┘
```

**Dependency rule:** No layer may bypass another layer. Application → API → Runtime → Plugins → Modules → Providers → Platform → Hardware.

---

## Module Map

```
drishti-sdk/
├── drishti-core/          # Layer 2-3: API + Runtime (interfaces, registry, pipeline, scene graph)
├── drishti-test/          # Shared test fixtures
├── drishti-vision/        # Layer 5-6: OpenCV image preprocessing
├── drishti-graph/         # Layer 5: Graph detection plugin
├── drishti-formula/       # Layer 5: Formula OCR plugin
├── drishti-molecule/      # Layer 5: Molecule detection + PubChem
├── drishti-haptics/       # Layer 5: Haptic rendering engine
├── drishti-audio/         # Layer 5: Spatial audio engine (Oboe)
├── drishti-voice/         # Layer 5: Voice assistant (Sherpa-ONNX)
├── drishti-android/       # Layer 7: Android platform integration (HAL + CameraX)
└── drishti-demo/          # Layer 1: Demo application
```

### Published modules (7 library JARs via maven-publish)

| Module | Artifact | Description |
|:---|:---|:---|
| `drishti-core` | `io.drishti:drishti-core` | Plugin interfaces, registry, pipeline, scene graph |
| `drishti-vision` | `io.drishti:drishti-vision` | OpenCV image preprocessing |
| `drishti-graph` | `io.drishti:drishti-graph` | Graph/chart detection |
| `drishti-formula` | `io.drishti:drishti-formula` | Formula OCR → LaTeX parsing |
| `drishti-molecule` | `io.drishti:drishti-molecule` | Chemical structure detection + PubChem |
| `drishti-haptics` | `io.drishti:drishti-haptics` | Haptic vibration rendering |
| `drishti-audio` | `io.drishti:drishti-audio` | Spatial audio sonification |
| `drishti-voice` | `io.drishti:drishti-voice` | Text-to-speech output |
| `drishti-android` | `io.drishti:drishti-android` | Android HAL + CameraX |

---

## Data Flow

```
Input (Camera/Bitmap/File)
        │
        ▼
┌───────────────────┐
│  Frame             │  Raw image data + metadata
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│  Pipeline.detect() │  Runs all DetectorPlugins concurrently via coroutineScope
└─────────┬─────────┘
          │ List<ContentItem>
          ▼
┌───────────────────┐
│  Pipeline.         │  Builds SceneGraph with spatial positions,
│  buildSceneGraph() │  edges (spatial, semantic, temporal, containment)
└─────────┬─────────┘
          │ SceneGraph
          ▼
┌───────────────────┐
│  DrishtiDiagram    │  Public API surface: .haptics() .audio() .voice() .explore()
└─────────┬─────────┘
          │
          ▼
    HapticOutput / AudioOutput / VoiceOutput / ExplorationSession
```

---

## Core Types (drishti-core)

### Public API Surface

| Type | Role |
|:---|:---|
| `Drishti` | Main entry point. Builder pattern for plugin registration |
| `DrishtiDiagram` | Processed diagram with `.haptics()`, `.audio()`, `.voice()`, `.explore()` |
| `Frame` | Input image: width, height, format, data, timestamp |
| `ContentItem` | Interface for all detected content (GraphContent, FormulaContent, etc.) |
| `ContentType` | Enum: GRAPH, FORMULA, MOLECULE, SHAPE, TABLE, TEXT, CUSTOM |
| `SceneGraph` | Nodes + Edges + Bounds. Indexed for O(1) lookups |
| `ExplorationSession` | Thread-safe interactive navigation through content items |

### Plugin Interfaces

| Interface | Method | Returns |
|:---|:---|:---|
| `DetectorPlugin` | `suspend fun detect(frame: Frame)` | `ContentItem?` |
| `HapticsRenderer` | `fun renderHaptic(items, focusIndex)` | `HapticOutput` |
| `AudioRenderer` | `fun renderAudio(items, focusIndex)` | `AudioOutput` |
| `VoiceOutputRenderer` | `fun renderVoice(items, focusIndex)` | `VoiceOutput` |

### Output Types

| Type | Fields |
|:---|:---|
| `HapticOutput` | `pulses: List<HapticPulse>`, `pattern: String` |
| `HapticPulse` | `intensity`, `duration`, `x`, `y`, `delay` (validated 0.0–1.0) |
| `AudioOutput` | `sources: List<AudioSource>`, `spatial: Boolean` |
| `AudioSource` | `frequency` (20–20kHz), `amplitude`, `spatialX/Y/Z` |
| `VoiceOutput` | `speech: SpeechSegment`, `language: String` |
| `TextOutput` | `text: String` |

### Scene Graph Structure

```
SceneGraph
├── nodes: List<SceneNode>          # Positioned content nodes
│   ├── DataPointNode               # x, y coordinates
│   ├── AxisNode                    # Axis with label and range
│   ├── TextNode                    # Text content (formula, name)
│   └── ShapeNode                   # Geometric shapes
├── edges: List<SceneEdge>          # Typed, weighted connections
│   ├── SPATIAL                     # Proximity-based
│   ├── CONTAINS                    # Bounding box overlap
│   ├── SEMANTIC                    # Content-type complementarity
│   └── TEMPORAL                    # Detection order
└── bounds: SceneBounds             # Scene dimensions
```

---

## Plugin Registration Flow

```kotlin
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())        // DetectorPlugin
    .addDetector(FormulaPlugin())      // DetectorPlugin
    .addRenderer(HapticsPlugin())      // HapticsRenderer
    .addRenderer(AudioPlugin())        // AudioRenderer
    .addRenderer(VoicePlugin())        // VoiceOutputRenderer
    .build()

val diagram = drishti.readAsync(frame)
diagram.haptics()     // Result<HapticOutput>
diagram.audio()       // Result<AudioOutput>
diagram.voice()       // Result<VoiceOutput>
diagram.explore()     // ExplorationSession
```

---

## Pipeline Configuration

```kotlin
PipelineConfig(
    spatialThreshold: Float = 300f,       // Max distance for spatial edges
    containmentOverlapRatio: Float = 0.5f, // Min overlap for containment edges
    minConfidence: Float = 0.3f,          // Min detector confidence to keep item
    maxItemsPerFrame: Int = 50,           // Max content items per frame
    explorationElementLimit: Int = 100     // Max elements in exploration
)
```

---

## Error Handling

All public API methods return `Result<T>`:

```kotlin
diagram.haptics()   // Result<HapticOutput>
diagram.audio()     // Result<AudioOutput>
diagram.voice()     // Result<VoiceOutput>
```

Failures include:
- No renderer registered → `IllegalStateException`
- Rendering exception → wrapped in `Result.failure`

CancellationException is always re-thrown (never swallowed).

---

## Thread Safety

- `ExplorationSession` uses `Mutex` for concurrent coroutine access
- `PluginRegistry` uses `Lock` for thread-safe registration
- `Pipeline.detect()` uses `coroutineScope` + `async` for parallel detector execution
- `SceneGraph` builds adjacency indexes lazily at construction time

---

## KMP Architecture

```
commonMain/       # Interfaces + pure Kotlin fallback (functional but slow)
androidMain/      # Real platform implementations (OpenCV, ML Kit, CameraX)
```

Rules:
- expect API is minimal — only what's needed cross-platform
- commonMain implementation should be FUNCTIONAL even if slow
- androidMain implementation should be FAST (native libs, JNI)
- Platform-specific code MUST NOT leak above the Platform layer

---

## Future Extensions

The architecture MUST support adding new modules without modifying existing modules:

- Circuit Plugin, Map Plugin, Flowchart Plugin, Geometry Plugin
- Navigation Agent, AR Agent, Braille Agent
- Flutter/React Native bindings
- WebAssembly port
- Python bindings

Without changing the Runtime architecture.

---

## Verification Checklist

Before submitting code, verify:

- [ ] No architecture violations (layer dependencies respected)
- [ ] No circular dependencies between modules
- [ ] No direct model usage (always through provider interfaces)
- [ ] No platform-specific logic in commonMain
- [ ] Public APIs unchanged (or major version bump)
- [ ] Plugin implements interfaces correctly
- [ ] Unit tests updated
- [ ] Documentation updated
- [ ] Performance unchanged or improved
