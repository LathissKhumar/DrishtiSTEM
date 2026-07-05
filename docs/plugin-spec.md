# Plugin Specification

> Rules for developing, testing, and distributing Drishti SDK plugins.

---

## Plugin Types

| Plugin Type | Interface | Purpose |
|:---|:---|:---|
| **Detector** | `DetectorPlugin` | Analyzes frames, returns `ContentItem` |
| **Haptics Renderer** | `HapticsRenderer` | Converts content → vibration patterns |
| **Audio Renderer** | `AudioRenderer` | Converts content → spatial audio |
| **Voice Renderer** | `VoiceOutputRenderer` | Converts content → spoken text |

A single plugin class may implement multiple interfaces (e.g., both `DetectorPlugin` and `HapticsRenderer`).

---

## Plugin Interface Contracts

### DetectorPlugin

```kotlin
public interface DetectorPlugin {
    val contentType: ContentType   // Required: what this plugin detects
    val confidence: Float          // Required: confidence threshold 0.0-1.0
    suspend fun detect(frame: Frame): ContentItem?
}
```

**Rules:**
- MUST return `null` when nothing is detected (never throw on empty input)
- MUST return `ContentItem` with matching `contentType`
- MUST use `suspend` for potentially long-running detection
- MUST NOT throw `CancellationException` (let it propagate naturally)
- SHOULD filter results by own `confidence` threshold before returning
- SHOULD be idempotent — same frame produces same result

### HapticsRenderer

```kotlin
public interface HapticsRenderer : RendererPlugin {
    fun renderHaptic(items: List<ContentItem>, focusIndex: Int = 0): HapticOutput
    fun renderExplorationHaptic(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): HapticOutput
}
```

**Rules:**
- `renderHaptic` — renders all items at once, `focusIndex` highlights one
- `renderExplorationHaptic` — renders navigation feedback for a single item
- All `HapticPulse` values MUST be within validated ranges (intensity 0.0-1.0, duration > 0)
- SHOULD use shorter durations for exploration feedback (< 50ms)
- SHOULD increase intensity for focused items

### AudioRenderer

```kotlin
public interface AudioRenderer : RendererPlugin {
    fun renderAudio(items: List<ContentItem>, focusIndex: Int = 0): AudioOutput
    fun renderExplorationAudio(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): AudioOutput
}
```

**Rules:**
- `renderAudio` — renders spatial audio mix for all items
- `renderExplorationAudio` — renders navigation audio feedback
- Frequencies MUST be in audible range (20Hz-20kHz)
- Spatial coordinates MUST be 0.0-1.0

### VoiceOutputRenderer

```kotlin
public interface VoiceOutputRenderer : RendererPlugin {
    fun renderVoice(items: List<ContentItem>, focusIndex: Int = 0): VoiceOutput
    fun renderExplorationVoice(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int = -1
    ): VoiceOutput
}
```

**Rules:**
- MUST produce meaningful natural language text
- Rate and pitch MUST be within 0.1-3.0
- SHOULD be locale-aware (default: "en-US")
- SHOULD describe structure, not just raw data

---

## Content Type Registry

Plugins register their content type via `ContentType`:

| Built-in Types | Module |
|:---|:---|
| `GRAPH` | `drishti-graph` |
| `FORMULA` | `drishti-formula` |
| `MOLECULE` | `drishti-molecule` |
| `SHAPE` | `drishti-core` (built-in) |
| `TABLE` | `drishti-core` (built-in) |
| `TEXT` | `drishti-core` (built-in) |
| `CUSTOM` | Third-party plugins |

### Adding Custom Types

For new domain types, use `ContentType.CUSTOM("my-type")`:

```kotlin
class CircuitPlugin : DetectorPlugin {
    override val contentType = ContentType.CUSTOM("circuit")
    override val confidence = 0.5f
    
    override suspend fun detect(frame: Frame): ContentItem? {
        // Detection logic
        return CircuitContent(confidence = 0.8f)
    }
}
```

---

## Plugin Lifecycle

```
Registration          Detection              Rendering
─────────────         ──────────             ──────────
addDetector(plugin)   pipeline.detect()      renderer.renderHaptic()
    │                     │                       │
    ▼                     ▼                       ▼
PluginRegistry        async { plugin           plugin processes
stores reference      .detect(frame) }        ContentItem list
    │                     │                       │
    ▼                     ▼                       ▼
Available for         Returns ContentItem     Returns Output
pipeline execution    or null                 (Haptic/Audio/Voice)
```

---

## Plugin Distribution

Each plugin is a separate Gradle module:

```
drishti-graph/         # Graph detection plugin
drishti-formula/       # Formula OCR plugin
drishti-molecule/      # Molecule detection plugin
drishti-haptics/       # Haptic rendering plugin
drishti-audio/         # Spatial audio plugin
drishti-voice/         # Voice output plugin
```

### Publishing

Published via `maven-publish` to JitPack:

```kotlin
// build.gradle.kts (per-plugin module)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    // maven-publish applied by root build.gradle.kts
}

// Group: io.drishti
// Artifact: drishti-{module-name}
// Version: 1.0.0
```

### Consumer Integration

```kotlin
dependencies {
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-graph:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-haptics:1.0.0")
}
```

---

## Plugin Template

```kotlin
package io.drishti.myplugin

import io.drishti.core.*

/**
 * Detects [MY_CONTENT_TYPE] content in visual frames.
 */
class MyDetectorPlugin : DetectorPlugin {

    override val contentType: ContentType = ContentType.CUSTOM("my-content-type")
    override val confidence: Float = 0.5f

    override suspend fun detect(frame: Frame): ContentItem? {
        if (frame.data == null || frame.data.isEmpty()) return null

        // 1. Preprocess frame data
        // 2. Run detection algorithm
        // 3. Validate confidence threshold
        // 4. Return ContentItem or null

        return null  // when nothing detected
    }
}

/**
 * Renders haptic feedback for [MY_CONTENT_TYPE] content.
 */
class MyHapticsPlugin : HapticsRenderer {

    override val name: String = "MyHaptics"

    override fun renderHaptic(
        items: List<ContentItem>,
        focusIndex: Int
    ): HapticOutput {
        val pulses = mutableListOf<HapticPulse>()
        items.forEachIndexed { index, item ->
            // Convert item to vibration patterns
            // Higher intensity for focused item
        }
        return HapticOutput(pulses = pulses, pattern = "my-pattern")
    }

    override fun renderExplorationHaptic(
        item: ContentItem,
        direction: ExplorationDirection,
        elementIndex: Int
    ): HapticOutput {
        return HapticOutput(
            pulses = listOf(
                HapticPulse(
                    intensity = 0.5f,
                    duration = 50L,
                    x = 0.5f,
                    y = 0.5f
                )
            ),
            pattern = "exploration-tap"
        )
    }
}
```

---

## Plugin Testing

Every plugin MUST have unit tests:

### Detector Tests

```kotlin
class MyDetectorPluginTest {
    private val plugin = MyDetectorPlugin()

    @Test
    fun WHEN_empty_frame THEN_returns_null() = runTest {
        val frame = Frame(0, 0, FrameFormat.RGB_888, null)
        assertNull(plugin.detect(frame))
    }

    @Test
    fun WHEN_valid_content THEN_returns_content_item() = runTest {
        val frame = TestFixtures.frameWithMyContent()
        val result = plugin.detect(frame)
        assertNotNull(result)
        assertEquals(ContentType.CUSTOM("my-content-type"), result!!.contentType)
        assertTrue(result.confidence >= plugin.confidence)
    }

    @Test
    fun WHEN_unsupported_format THEN_returns_null() = runTest {
        val frame = Frame(100, 100, FrameFormat.GRAYSCALE, ByteArray(100))
        val result = plugin.detect(frame)
        // Either returns null or low-confidence item
    }
}
```

### Renderer Tests

```kotlin
class MyHapticsPluginTest {
    private val plugin = MyHapticsPlugin()

    @Test
    fun WHEN_render_haptic THEN_returns_valid_output() {
        val item = MyContent(confidence = 0.8f)
        val output = plugin.renderHaptic(listOf(item), focusIndex = 0)
        assertNotNull(output)
        assertTrue(output.pulses.isNotEmpty())
    }

    @Test
    fun WHEN_exploration THEN_returns_feedback() {
        val item = MyContent(confidence = 0.8f)
        val output = plugin.renderExplorationHaptic(item, ExplorationDirection.NEXT)
        assertNotNull(output)
    }
}
```

---

## Quality Checklist

Before releasing a plugin:

- [ ] Implements correct interfaces (`DetectorPlugin` / `HapticsRenderer` / etc.)
- [ ] `contentType` matches `ContentItem.contentType`
- [ ] `detect()` returns `null` for empty/unsupported frames
- [ ] No `!!` operators
- [ ] No broad exception catching
- [ ] KDoc on all public APIs
- [ ] Unit tests covering happy path, error path, edge cases
- [ ] API dump file updated (`api/my-plugin.api`)
- [ ] No hardcoded values (thresholds configurable)
- [ ] Thread-safe (if stateful)
