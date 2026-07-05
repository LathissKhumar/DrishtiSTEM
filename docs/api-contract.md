# API Contract

> Public SDK interfaces — the stable API surface that applications depend on. Breaking changes require a major version bump.

---

## Stability Rules

| Scope | Stability | Breaking change policy |
|:---|:---|:---|
| `drishti-core` public types | **Stable** | Major version bump required |
| `drishti-*-plugin` public types | **Stable** | Major version bump required |
| `drishti-android` public types | **Stable** | Major version bump required |
| Internal implementation details | **Unstable** | Can change in any release |
| `drishti-test` fixtures | **Unstable** | Test utilities may change |

Public API is tracked via Binary Compatibility Validator (BCV). API dump files in `api/` directories MUST match compiled output.

---

## Public API Surface

### Entry Point: `Drishti`

```kotlin
package io.drishti.core

public class Drishti private constructor(
    detectors: List<DetectorPlugin>,
    renderers: List<RendererPlugin>,
    pipeline: Pipeline
) {
    /** Read visual content from a frame (suspend). */
    public suspend fun readAsync(frame: Frame): DrishtiDiagram

    public class Builder {
        public fun addDetector(plugin: DetectorPlugin): Builder
        public fun addRenderer(plugin: RendererPlugin): Builder
        public fun build(): Drishti
    }
}
```

**Usage:**
```kotlin
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .build()

val diagram = drishti.readAsync(frame)
```

**Constraints:**
- At least one `DetectorPlugin` must be registered (`require` in `build()`)
- `Pipeline` is created internally with default `PipelineConfig`

---

### Result Type: `DrishtiDiagram`

```kotlin
package io.drishti.core

public class DrishtiDiagram(
    public val contentItems: List<ContentItem>,
    public val sceneGraph: SceneGraph,
    renderers: List<RendererPlugin>,
    pipeline: Pipeline
) {
    /** Render haptic feedback. Returns Result<HapticOutput>. */
    public fun haptics(): Result<HapticOutput>

    /** Render spatial audio. Returns Result<AudioOutput>. */
    public fun audio(): Result<AudioOutput>

    /** Generate spoken description. Returns Result<VoiceOutput>. */
    public fun voice(): Result<VoiceOutput>

    /** Get text summary. */
    public fun summary(): TextOutput

    /** Interactive exploration mode. */
    public fun explore(): ExplorationSession
}
```

**Error conditions:**
- No renderer registered → `Result.failure(IllegalStateException)`
- Rendering exception → `Result.failure(exception)`
- `CancellationException` → always re-thrown (never swallowed)

---

### Input: `Frame`

```kotlin
@Serializable
public data class Frame(
    val width: Int,              // Must be > 0
    val height: Int,             // Must be > 0
    val format: FrameFormat,
    val data: ByteArray? = null,
    val timestamp: Long = 0L
)

@Serializable
public enum class FrameFormat {
    YUV_420_888,
    RGB_888,
    GRAYSCALE,
    JPEG,
    PNG
}
```

**Constraints:**
- `width` and `height` must be positive (enforced by `init` block)

---

### Content Types

#### ContentItem Interface

```kotlin
public interface ContentItem {
    public val contentType: ContentType
    public val confidence: Float
}
```

#### ContentType Enum

```kotlin
@Serializable
public enum class ContentType {
    GRAPH,
    FORMULA,
    MOLECULE,
    SHAPE,
    TABLE,
    TEXT,
    CUSTOM
}
```

#### GraphContent

```kotlin
public data class GraphContent(
    val graphType: GraphType,
    val title: String = "",
    val axes: Axes = Axes(),
    val dataPoints: List<DataPoint> = emptyList(),
    val labels: List<String> = emptyList(),
    override val confidence: Float
) : ContentItem
```

#### FormulaContent

```kotlin
public interface FormulaContentItem : ContentItem {
    val formulaType: FormulaType
    val expression: String
    val symbols: List<FormulaSymbol>
    val geometry: Geometry?
}

public data class FormulaContent(
    override val formulaType: FormulaType,
    override val expression: String,
    override val symbols: List<FormulaSymbol> = emptyList(),
    override val geometry: Geometry? = null,
    override val confidence: Float
) : FormulaContentItem
```

#### MoleculeContent

```kotlin
public data class MoleculeContent(
    val moleculeType: MoleculeType,
    val atoms: List<Atom> = emptyList(),
    val bonds: List<Bond> = emptyList(),
    val name: String = "",
    val geometry: Geometry? = null,
    val cid: Int = 0,
    val molecularFormula: String = "",
    val molecularWeight: Double = 0.0,
    val iupacName: String = "",
    val canonicalSmiles: String = "",
    val inchiKey: String = "",
    override val confidence: Float
) : ContentItem
```

#### ShapeContent

```kotlin
public data class ShapeContent(
    val shapeType: ShapeType,
    val area: Float,
    val perimeter: Float,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    override val confidence: Float
) : ContentItem
```

#### TableContent

```kotlin
public data class TableContent(
    val rows: Int,
    val columns: Int,
    val cells: List<List<String>>,
    override val confidence: Float
) : ContentItem
```

---

### Plugin Interfaces

#### DetectorPlugin

```kotlin
public interface DetectorPlugin {
    /** The type of content this plugin detects. */
    public val contentType: ContentType

    /** Confidence threshold for this detector. */
    public val confidence: Float

    /** Detect content in the given frame. */
    public suspend fun detect(frame: Frame): ContentItem?
}
```

#### RendererPlugin (base)

```kotlin
public interface RendererPlugin {
    public val name: String
}
```

#### HapticsRenderer

```kotlin
public interface HapticsRenderer : RendererPlugin {
    public fun renderHaptic(
        items: List<ContentItem>,
        focusIndex: Int = 0
    ): HapticOutput

    public fun renderExplorationHaptic(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): HapticOutput
}
```

#### AudioRenderer

```kotlin
public interface AudioRenderer : RendererPlugin {
    public fun renderAudio(
        items: List<ContentItem>,
        focusIndex: Int = 0
    ): AudioOutput

    public fun renderExplorationAudio(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): AudioOutput
}
```

#### VoiceOutputRenderer

```kotlin
public interface VoiceOutputRenderer : RendererPlugin {
    public fun renderVoice(
        items: List<ContentItem>,
        focusIndex: Int = 0
    ): VoiceOutput

    public fun renderExplorationVoice(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): VoiceOutput
}
```

---

### Output Types

```kotlin
@Serializable
public data class HapticOutput(
    val pulses: List<HapticPulse>,
    val pattern: String = ""
)

@Serializable
public data class HapticPulse(
    val intensity: Float,  // 0.0-1.0
    val duration: Long,    // milliseconds, must be > 0
    val x: Float = 0.5f,  // 0.0-1.0
    val y: Float = 0.5f,  // 0.0-1.0
    val delay: Long = 0L  // must be >= 0
)

@Serializable
public data class AudioOutput(
    val sources: List<AudioSource>,
    val spatial: Boolean = true
)

@Serializable
public data class AudioSource(
    val frequency: Float,    // 20-20000 Hz
    val amplitude: Float,    // 0.0-1.0
    val spatialX: Float = 0.5f,
    val spatialY: Float = 0.5f,
    val spatialZ: Float = 0.5f
)

@Serializable
public data class VoiceOutput(
    val speech: SpeechSegment,
    val language: String = "en-US"
)

@Serializable
public data class SpeechSegment(
    val text: String,
    val rate: Float = 1.0f,   // 0.1-3.0
    val pitch: Float = 1.0f   // 0.1-3.0
)
```

---

### Scene Graph Types

```kotlin
@Serializable
public data class SceneGraph(
    val nodes: List<SceneNode>,
    val edges: List<SceneEdge>,
    val bounds: SceneBounds = SceneBounds(0f, 0f),
    val metadata: Map<String, String> = emptyMap()
) {
    fun describe(): String
    fun nodeById(id: String): SceneNode?
    fun edgesFor(nodeId: String): List<SceneEdge>
    fun neighbors(nodeId: String): List<String>
}

@Serializable
public sealed class SceneNode {
    abstract val id: String
    abstract val position: Point
    open val depth: Int get() = 0

    data class DataPointNode(...)
    data class AxisNode(...)
    data class TextNode(...)
    data class ShapeNode(...)
}

@Serializable
public data class SceneEdge(
    val sourceId: String,
    val targetId: String,
    val edgeType: EdgeType,
    val weight: Float = 1.0f
)

@Serializable
public enum class EdgeType {
    CONTAINS, CONNECTS, LABELS, MEASURES,
    SPATIAL, SEMANTIC, TEMPORAL
}
```

---

### Exploration Types

```kotlin
public enum class ExplorationDirection {
    NEXT,
    PREVIOUS,
    POSITION
}

public sealed class ExplorationResult {
    data class Item(val item: ContentItem, val description: String) : ExplorationResult()
    data object End : ExplorationResult()
    data object Beginning : ExplorationResult()
}

public data class ExplorationPosition(
    val current: Int,
    val total: Int
)

public class ExplorationSession(
    contentItems: List<ContentItem>,
    renderers: List<RendererPlugin>
) {
    suspend fun next(): ExplorationResult
    suspend fun previous(): ExplorationResult
    suspend fun nextElement(): ExplorationResult
    suspend fun previousElement(): ExplorationResult
    suspend fun position(): ExplorationPosition
    suspend fun elementPosition(): ExplorationPosition
    suspend fun haptic(): HapticOutput?
    suspend fun audio(): AudioOutput?
    suspend fun voice(): VoiceOutput?
    suspend fun exploreHaptic(direction: ExplorationDirection): HapticOutput?
    suspend fun exploreAudio(direction: ExplorationDirection): AudioOutput?
    suspend fun exploreVoice(direction: ExplorationDirection): VoiceOutput?
}
```

---

### Geometry Types

```kotlin
@Serializable
public data class Point(val x: Float, val y: Float)

@Serializable
public data class BoundingBox(
    val x: Float, val y: Float,
    val width: Float, val height: Float
)

@Serializable
public data class Axes(val x: Axis = Axis(), val y: Axis = Axis())

@Serializable
public data class Axis(
    val label: String = "",
    val range: ClosedFloatingPointRange<Float> = 0f..100f
)

@Serializable
public data class DataPoint(
    val x: Float, val y: Float,
    val label: String? = null
)

@Serializable
public data class Geometry(
    val points: List<Point> = emptyList(),
    val boundingBox: BoundingBox? = null
)
```

---

### Configuration

```kotlin
@Serializable
public data class PipelineConfig(
    val spatialThreshold: Float = 300f,
    val containmentOverlapRatio: Float = 0.5f,
    val minConfidence: Float = 0.3f,
    val maxItemsPerFrame: Int = 50,
    val explorationElementLimit: Int = 100
)
```

---

## API Change Policy

1. **Additive changes** (new types, new methods with defaults) — patch version
2. **Behavioral changes** (same signature, different output) — minor version
3. **Breaking changes** (removed types, changed signatures) — major version
4. **Experimental APIs** — annotate with `@ExperimentalApi`, may change in any release
