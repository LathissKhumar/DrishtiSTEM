# Event System

> Event definitions and data flow in the Drishti SDK pipeline.

---

## Overview

The Drishti SDK uses a pipeline-based event flow. Modules communicate through the `Pipeline` orchestrator rather than direct cross-module calls. Each stage of the pipeline produces typed outputs consumed by the next stage.

```
Frame Input
    │
    ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Pipeline    │───▶│  Detectors  │───▶│  SceneGraph  │
│  .detect()   │    │  (parallel)  │    │  Builder     │
└─────────────┘    └─────────────┘    └─────────────┘
                         │                    │
                         ▼                    ▼
                    ContentItem          SceneGraph
                         │                    │
                         ▼                    ▼
                   ┌──────────────────────────────┐
                   │       DrishtiDiagram          │
                   │  .haptics() .audio() .voice() │
                   └──────────────────────────────┘
```

---

## Pipeline Events

### Stage 1: Frame Input

| Event | Type | Source | Consumer |
|:---|:---|:---|:---|
| Frame captured | `Frame` | Application | `Pipeline.detect()` |

```kotlin
data class Frame(
    val width: Int,
    val height: Int,
    val format: FrameFormat,
    val data: ByteArray?,
    val timestamp: Long
)
```

### Stage 2: Detection

| Event | Type | Source | Consumer |
|:---|:---|:---|:---|
| Detection requested | `Frame` | `Pipeline` | Each `DetectorPlugin` |
| Detection complete | `ContentItem?` | Each `DetectorPlugin` | `Pipeline` |

**Parallel execution:**
```kotlin
coroutineScope {
    detectors.map { detector ->
        async {
            try {
                detector.detect(frame)
            } catch (_: CancellationException) {
                throw CancellationException("Pipeline cancelled during detection")
            } catch (e: Exception) {
                when (e) {
                    is IllegalStateException,
                    is IllegalArgumentException,
                    is UnsupportedOperationException -> throw e
                    else -> null  // non-fatal detector errors swallowed
                }
            }
        }
    }.awaitAll()
        .filterNotNull()
        .filter { it.confidence >= config.minConfidence }
}
```

**Content types produced:**

| ContentItem Subtype | ContentType | Fields |
|:---|:---|:---|
| `GraphContent` | `GRAPH` | graphType, title, axes, dataPoints, labels |
| `FormulaContent` | `FORMULA` | formulaType, expression, symbols, geometry |
| `MoleculeContent` | `MOLECULE` | moleculeType, atoms, bonds, name, cid |
| `ShapeContent` | `SHAPE` | shapeType, area, perimeter, position |
| `TableContent` | `TABLE` | rows, columns, cells |

### Stage 3: Scene Graph Construction

| Event | Type | Source | Consumer |
|:---|:---|:---|:---|
| Content items ready | `List<ContentItem>` | `Pipeline` | `buildSceneGraph()` |
| Scene graph built | `SceneGraph` | `buildSceneGraph()` | `DrishtiDiagram` |

**Edge types generated:**

| EdgeType | Trigger | Weight Calculation |
|:---|:---|:---|
| `SPATIAL` | Nodes within `spatialThreshold` distance | `1 - (distance / threshold)` |
| `CONTAINS` | Bounding box overlap > `containmentOverlapRatio` | `overlap / smallerArea` |
| `SEMANTIC` | Complementary content types (FORMULA↔GRAPH, etc.) | `min(confidence_i, confidence_j) * proximity` |
| `TEMPORAL` | Sequential detection order | `0.5 + 0.5 * proximity` |

### Stage 4: Rendering

| Event | Type | Source | Consumer |
|:---|:---|:---|:---|
| Render haptic | `List<ContentItem>` | `DrishtiDiagram.haptics()` | `HapticsRenderer` |
| Haptic output | `HapticOutput` | `HapticsRenderer` | Application |
| Render audio | `List<ContentItem>` | `DrishtiDiagram.audio()` | `AudioRenderer` |
| Audio output | `AudioOutput` | `AudioRenderer` | Application |
| Render voice | `List<ContentItem>` | `DrishtiDiagram.voice()` | `VoiceOutputRenderer` |
| Voice output | `VoiceOutput` | `VoiceOutputRenderer` | Application |

---

## Exploration Events

The `ExplorationSession` supports interactive navigation through content items:

| Event | Direction | Return Type |
|:---|:---|:---|
| `next()` | Forward | `ExplorationResult.Item` / `ExplorationResult.End` |
| `previous()` | Backward | `ExplorationResult.Item` / `ExplorationResult.Beginning` |
| `nextElement()` | Forward within item | `ExplorationResult.Item` / `ExplorationResult.End` |
| `previousElement()` | Backward within item | `ExplorationResult.Item` / `ExplorationResult.Beginning` |

**Navigation state:**
- Thread-safe via `Mutex`
- Tracks `currentItemIndex` and `currentElementIndex`
- Element count varies by content type:
  - `GraphContent` → data points
  - `FormulaContent` → symbols
  - `MoleculeContent` → atoms

---

## Error Events

All public API methods that can fail return `Result<T>`:

| Error Condition | Error Type |
|:---|:---|
| No renderer registered | `IllegalStateException` |
| Rendering exception | Wrapped exception |
| Cancellation | Re-thrown (never swallowed) |
| Empty frame | Returns empty list (no error) |
| Detector failure | Returns null (non-fatal) |

---

## Concurrency Model

```kotlin
// Pipeline detection — parallel execution
coroutineScope {
    detectors.map { async { detector.detect(frame) } }.awaitAll()
}

// Exploration session — mutex-protected state
private val mutex = Mutex()
suspend fun next(): ExplorationResult = mutex.withLock { ... }

// SceneGraph — lazy index construction
private val nodeIndex: Map<String, SceneNode> by lazy { nodes.associateBy { it.id } }
```

---

## Future: Event Bus

The architecture spec defines a full event bus for agent-based communication. This is planned for Phase 2:

```
VisionComplete
OCRRequested
OCRFinished
SceneParsed
KnowledgeBuilt
AccessibilityReady
HapticsGenerated
VoiceGenerated
RenderingCompleted
```

Currently, communication is pipeline-based (functions return values). The event bus will enable decoupled agent communication without direct function calls.
