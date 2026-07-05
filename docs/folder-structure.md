# Folder Structure

> Directory responsibilities in the Drishti SDK. Every directory has exactly one responsibility.

---

## Root Layout

```
DrishtiSDK/                      # SDK source code repository
├── .audit/                      # Audit reports, research docs, session handoffs
│   ├── PRODUCTION_AUDIT_FINDINGS.md
│   ├── PROJECT_CONSTITUTION.md
│   ├── ROADMAP.md
│   └── research/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml               # CI pipeline (build + test + apiCheck)
│   │   └── release.yml          # Release pipeline (tag → build → GitHub Release)
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   └── PULL_REQUEST_TEMPLATE.md
├── drishti-core/                # Core SDK: interfaces, registry, pipeline, scene graph
│   ├── src/
│   │   ├── commonMain/          # KMP common code (all platforms)
│   │   └── androidMain/         # Android-specific implementations
│   └── api/                     # Binary compatibility dump
├── drishti-test/                # Shared test fixtures
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-vision/              # Image preprocessing (OpenCV)
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-graph/               # Graph detection plugin
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-formula/             # Formula OCR plugin
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-molecule/            # Molecule detection + PubChem plugin
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-haptics/             # Haptic rendering engine
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-audio/               # Spatial audio engine (Oboe)
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-voice/               # Voice assistant (Sherpa-ONNX)
│   └── src/
│       ├── commonMain/
│       └── androidMain/
├── drishti-android/             # Android platform integration (HAL + CameraX)
│   └── src/
│       └── androidMain/
├── drishti-demo/                # Demo application
│   └── src/
│       └── androidMain/
├── build.gradle.kts             # Root build: plugins, maven-publish, dokka
├── settings.gradle.kts          # Module includes
├── gradle/
│   └── libs.versions.toml       # Version catalog
├── CHANGELOG.md
├── LICENSE
├── README.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
└── SECURITY.md
```

---

## Module Responsibilities

### `drishti-core` — Core SDK

**Package:** `io.drishti.core`

Contains all public interfaces, data types, and the runtime orchestrator:

| File | Responsibility |
|:---|:---|
| `Drishti.kt` | Main entry point, Builder pattern |
| `DrishtiDiagram.kt` | Processed diagram with `.haptics()` `.audio()` `.voice()` `.explore()` |
| `Pipeline.kt` | Orchestrates detection + scene graph construction |
| `PipelineConfig.kt` | Configuration parameters (thresholds, limits) |
| `Registry.kt` | Plugin registration and validation |
| `PluginRegistry.kt` | (alias for Registry) |
| `ContentItem.kt` | Content data types (GraphContent, FormulaContent, MoleculeContent, etc.) |
| `ContentType.kt` | ContentType enum |
| `DetectorPlugin.kt` | Detector plugin interface |
| `RendererPlugin.kt` | Renderer plugin interfaces (HapticsRenderer, AudioRenderer, VoiceOutputRenderer) |
| `SceneGraph.kt` | SceneGraph, SceneNode, SceneEdge, EdgeType |
| `EdgeGenerators.kt` | Edge generation algorithms (spatial, containment, semantic, temporal) |
| `Frame.kt` | Input frame type |
| `Geometry.kt` | Point, BoundingBox, Axes, DataPoint, Geometry |
| `Output.kt` | Output types (HapticOutput, AudioOutput, VoiceOutput, TextOutput) |
| `ExplorationSession.kt` | Interactive navigation through content items |
| `Lock.kt` | Thread-safe lock utility |
| `BoundingBoxUtils.kt` | Bounding box computation utilities |
| `NodeBuilders.kt` | SceneNode construction from ContentItems |

### `drishti-test` — Test Fixtures

Shared test utilities and fixtures. Unstable — may change between releases.

### `drishti-vision` — Image Preprocessing

OpenCV-based image preprocessing pipeline. Provides:
- Image normalization
- Feature extraction
- Frame format conversion

### `drishti-graph` — Graph Detection

Detects line charts, scatter plots, bar charts, function plots. Extracts:
- Axes labels and ranges
- Data points
- Trend lines
- Intersections

### `drishti-formula` — Formula OCR

LaTeX formula detection and parsing:
- Formula type classification (algebraic, trigonometric, calculus)
- Symbol extraction with positions
- Expression parsing
- Geometry estimation

### `drishti-molecule` — Molecule Detection

Chemical structure detection and PubChem enrichment:
- SMILES parsing
- Atom/bond graph construction
- PubChem CID lookup
- Molecular properties (formula, weight, IUPAC name)

### `drishti-haptics` — Haptic Rendering

Converts content items to vibration patterns:
- VibrationEffect.Composition (API 30+)
- Waveform fallback (pre-API 30)
- Exploration haptic feedback

### `drishti-audio` — Spatial Audio

Spatial audio sonification:
- Oboe low-latency audio engine
- HRTF spatialization
- Data point → frequency mapping
- Exploration audio feedback

### `drishti-voice` — Voice Output

Text-to-speech output:
- Sherpa-ONNX offline TTS/STT
- Spoken description generation
- Exploration voice feedback

### `drishti-android` — Android Platform

Android-specific integration:
- CameraX integration
- Hardware Abstraction Layer (HAL)
- Device capability detection

### `drishti-demo` — Demo Application

Showcase app demonstrating SDK capabilities. Not published.

---

## Source Layout (per module)

```
drishti-{module}/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── io/drishti/{module}/
│   │           ├── *.kt              # Implementation files
│   │           └── internal/         # Internal implementation (not public API)
│   ├── androidMain/
│   │   └── kotlin/
│   │       └── io/drishti/{module}/
│   │           └── *.kt              # Android-specific implementations
│   ├── commonTest/
│   │   └── kotlin/
│   │       └── io/drishti/{module}/
│   │           └── *Test.kt          # Unit tests
│   └── androidUnitTest/
│       └── kotlin/
│           └── io/drishti/{module}/
│               └── *Test.kt          # Android unit tests
├── build.gradle.kts                   # Module build config
└── api/
    └── api.bc                         # Binary compatibility dump
```

---

## Package Naming

All packages follow: `io.drishti.{module}`

| Module | Package |
|:---|:---|
| drishti-core | `io.drishti.core` |
| drishti-vision | `io.drishti.vision` |
| drishti-graph | `io.drishti.graph` |
| drishti-formula | `io.drishti.formula` |
| drishti-molecule | `io.drishti.molecule` |
| drishti-haptics | `io.drishti.haptics` |
| drishti-audio | `io.drishti.audio` |
| drishti-voice | `io.drishti.voice` |
| drishti-android | `io.drishti.android` |

---

## Files That Should Never Change

| File | Why |
|:---|:---|
| `build.gradle.kts` (root) | maven-publish config — changes break publishing |
| `settings.gradle.kts` | Module includes — changes break builds |
| `gradle/libs.versions.toml` | Version catalog — changes affect all modules |
| `api/*.api` files | Binary compatibility — changes require version bump |

---

## Files That Change Often

| File | Why |
|:---|:---|
| `drishti-*/src/**/*.kt` | Implementation code |
| `drishti-*/src/*Test/*.kt` | Tests |
| `CHANGELOG.md` | Release notes |
| `.github/workflows/*.yml` | CI/CD configuration |
