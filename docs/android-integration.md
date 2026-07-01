# Android Integration Guide

This guide explains how to integrate the Drishti SDK into Android applications.

## Setup

### Gradle Configuration

Add the Drishti SDK dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    // Core SDK
    implementation("io.drishti:drishti-core:1.0.0")
    
    // Plugins (pick what you need)
    implementation("io.drishti:drishti-graph:1.0.0")
    implementation("io.drishti:drishti-formula:1.0.0")
    implementation("io.drishti:drishti-molecule:1.0.0")
    
    // Renderers
    implementation("io.drishti:drishti-haptics:1.0.0")
    implementation("io.drishti:drishti-audio:1.0.0")
    implementation("io.drishti:drishti-voice:1.0.0")
    
    // Android integration
    implementation("io.drishti:drishti-android:1.0.0")
}
```

### Permissions

Add required permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
```

## Usage

### Basic Usage

```kotlin
import io.drishti.core.Drishti
import io.drishti.graph.GraphPlugin
import io.drishti.haptics.HapticsPlugin

class MainActivity : AppCompatActivity() {
    
    private lateinit var drishti: Drishti
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        drishti = Drishti.Builder()
            .addDetector(GraphPlugin())
            .addRenderer(HapticsPlugin())
            .build()
    }
    
    fun processImage(bitmap: Bitmap) {
        // Convert bitmap to frame
        val frame = Frame(
            width = bitmap.width,
            height = bitmap.height,
            format = FrameFormat.RGB_888,
            data = bitmapToByteArray(bitmap)
        )
        
        // Process frame
        val diagram = drishti.read(frame)
        
        // Get outputs
        val haptics = diagram.haptics()
        val audio = diagram.audio()
        val voice = diagram.voice()
    }
}
```

### Camera Integration

```kotlin
import io.drishti.android.CameraCapture

class CameraActivity : AppCompatActivity() {
    
    private lateinit var cameraCapture: CameraCapture
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        cameraCapture = CameraCapture(this)
        cameraCapture.startCapture { frame ->
            val diagram = drishti.read(frame)
            // Process output...
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraCapture.stopCapture()
    }
}
```

### Haptic Feedback

```kotlin
import io.drishti.android.HapticHAL

fun playHapticOutput(output: HapticOutput) {
    val hapticHAL = HapticHAL(context)
    hapticHAL.play(output)
}
```

### Audio Output

```kotlin
import io.drishti.android.AudioHAL

fun playAudioOutput(output: AudioOutput) {
    val audioHAL = AudioHAL(context)
    audioHAL.play(output)
}
```

## Device Compatibility

The SDK requires:
- Android API 30 (Android 11) or higher
- Camera with autofocus (for camera integration)
- Vibrator with amplitude control (for haptic feedback)

## Performance Tips

1. **Use background threads** - Process frames off the main thread
2. **Batch processing** - Process multiple frames in sequence
3. **Reuse Drishti instance** - Don't create a new instance for each frame
4. **Monitor memory** - Release frames after processing
