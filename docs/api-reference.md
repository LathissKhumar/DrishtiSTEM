# API Reference

## Core Classes

### Drishti

Main entry point for the SDK.

```kotlin
class Drishti private constructor(
    private val detectors: List<DetectorPlugin>,
    private val renderers: List<RendererPlugin>
) {
    class Builder {
        fun addDetector(plugin: DetectorPlugin): Builder
        fun addRenderer(plugin: RendererPlugin): Builder
        fun build(): Drishti
    }
    
    fun read(frame: Frame): DrishtiDiagram
}
```

### DrishtiDiagram

Result of processing a frame.

```kotlin
class DrishtiDiagram(
    private val items: List<ContentItem>,
    private val renderers: List<RendererPlugin>
) {
    fun haptics(): HapticOutput
    fun audio(): AudioOutput
    fun voice(): VoiceOutput
    fun explore(): ExplorationSession
}
```

## Content Types

### Frame

```kotlin
data class Frame(
    val width: Int,      // Must be > 0
    val height: Int,     // Must be > 0
    val format: FrameFormat,
    val data: ByteArray?
)
```

### GraphContent

```kotlin
data class GraphContent(
    val graphType: GraphType,
    val title: String,
    val axes: Axes,
    val dataPoints: List<DataPoint>,
    val labels: List<String>
) : ContentItem
```

### FormulaContent

```kotlin
data class FormulaContent(
    val formulaType: FormulaType,
    val expression: String,
    val symbols: List<FormulaSymbol>,
    val geometry: Geometry?
) : ContentItem
```

### MoleculeContent

```kotlin
data class MoleculeContent(
    val moleculeType: MoleculeType,
    val atoms: List<Atom>,
    val bonds: List<Bond>,
    val name: String,
    val geometry: Geometry?
) : ContentItem
```

## Output Types

### HapticOutput

```kotlin
data class HapticOutput(
    val pulses: List<HapticPulse>,
    val pattern: String
)
```

### AudioOutput

```kotlin
data class AudioOutput(
    val sources: List<AudioSource>,
    val spatial: Boolean
)
```

### VoiceOutput

```kotlin
data class VoiceOutput(
    val speech: SpeechSegment,
    val language: String
)
```

## Plugin Interfaces

### DetectorPlugin

```kotlin
interface DetectorPlugin {
    val contentType: ContentType
    fun detect(frame: Frame): List<ContentItem>
}
```

### HapticsRenderer

```kotlin
interface HapticsRenderer {
    fun renderHaptics(item: ContentItem): HapticOutput
}
```

### AudioRenderer

```kotlin
interface AudioRenderer {
    fun renderAudio(item: ContentItem): AudioOutput
}
```

### VoiceOutputRenderer

```kotlin
interface VoiceOutputRenderer {
    fun renderVoice(item: ContentItem): VoiceOutput
}
```
