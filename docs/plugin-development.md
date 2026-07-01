# Plugin Development Guide

This guide explains how to create custom plugins for the Drishti SDK.

## Plugin Structure

A plugin implements both detection and rendering interfaces:

```kotlin
package io.drishti.myplugin

import io.drishti.core.*

class MyPlugin : DetectorPlugin, HapticsRenderer, AudioRenderer, VoiceOutputRenderer {
    
    override val contentType = ContentType.CUSTOM("my-content-type")
    
    // Detection
    override fun detect(frame: Frame): List<ContentItem> {
        // Analyze frame and return detected content
        return listOf(
            MyContent(
                // ... content data
            )
        )
    }
    
    // Haptic Rendering
    override fun renderHaptics(item: ContentItem): HapticOutput {
        val content = item as MyContent
        return HapticOutput(
            pulses = listOf(
                HapticPulse(intensity = 0.5f, duration = 100L, x = 0.5f, y = 0.5f)
            ),
            pattern = "my-pattern"
        )
    }
    
    // Audio Rendering
    override fun renderAudio(item: ContentItem): AudioOutput {
        val content = item as MyContent
        return AudioOutput(
            sources = listOf(
                AudioSource(frequency = 440f, amplitude = 0.5f, spatialX = 0.5f, spatialY = 0.5f, spatialZ = 0.5f)
            ),
            spatial = true
        )
    }
    
    // Voice Rendering
    override fun renderVoice(item: ContentItem): VoiceOutput {
        val content = item as MyContent
        return VoiceOutput(
            speech = SpeechSegment(
                text = "Description of the content",
                rate = 1.0f,
                pitch = 1.0f
            ),
            language = "en-US"
        )
    }
}
```

## Registration

Register your plugin with the SDK:

```kotlin
val drishti = Drishti.Builder()
    .addDetector(MyPlugin())
    .build()
```

## Content Types

Define your content type:

```kotlin
data class MyContent(
    // Your content data
) : ContentItem {
    override val type = ContentType.CUSTOM("my-content-type")
}
```

## Best Practices

1. **Keep plugins focused** - One content type per plugin
2. **Use sensible defaults** - Provide default values for optional parameters
3. **Handle errors gracefully** - Return empty lists for unsupported content
4. **Write tests** - Cover edge cases and error conditions
5. **Document public APIs** - Use KDoc for all public methods

## Testing

Test your plugin:

```kotlin
class MyPluginTest {
    @Test
    fun detectReturnsContent() {
        val plugin = MyPlugin()
        val frame = TestFixtures.frame()
        val items = plugin.detect(frame)
        assertTrue(items.isNotEmpty())
    }
    
    @Test
    fun renderHapticsReturnsOutput() {
        val plugin = MyPlugin()
        val item = MyContent(...)
        val output = plugin.renderHaptics(item)
        assertNotNull(output)
        assertTrue(output.pulses.isNotEmpty())
    }
}
```
