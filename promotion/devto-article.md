# Drishti SDK v1.0.0: Open-Source Accessibility Infrastructure for Visual STEM Content

*Making graphs, formulas, and molecules accessible through haptics, spatial audio, and voice.*

---

## The Problem

STEM education is inherently visual. A line chart conveys trends through position. A molecular structure shows relationships through bonds. An integral formula encodes meaning through spatial arrangement.

For blind and visually impaired users, this visual information is largely inaccessible. Screen readers excel at text but struggle with spatial relationships — they can tell you "there is a chart" but not that "the data trends upward from left to right with a peak at x=3."

Most existing accessibility tools focus on text-to-speech for written content. But STEM content needs a different approach: one that conveys spatial structure, relationships, and mathematical meaning through non-visual channels.

## The Solution: Drishti SDK

Drishti SDK is **accessibility infrastructure** — not an app. It's the layer that sits between your visual content and users who need non-visual access.

```
Visual Content (Image/PDF/Camera)
        ↓
   Drishti SDK (Plugin Pipeline)
        ↓
┌───────┴───────┬─────────┬──────────┐
↓               ↓         ↓          ↓
📳 Haptics   🔊 Spatial Audio   🗣️ Voice   📝 Text
```

**One SDK. Infinite plugins.** Developers write plugins for new content types. Users get accessibility automatically.

## Architecture

### Plugin-Based Detection

Every content type is a plugin. The core SDK knows nothing about graphs, formulas, or molecules — it only knows how to discover, register, and orchestrate plugins.

```kotlin
// Write a custom plugin
class CircuitPlugin : DetectorPlugin {
    override val contentType = ContentType.CUSTOM("circuit")
    
    override suspend fun detect(frame: Frame): List<ContentItem> {
        // Your detection logic
        return listOf(ContentItem(...))
    }
}

// Register it
Drishti.Builder()
    .addDetector(CircuitPlugin())
    .build()
```

### Pipeline Processing

The SDK processes visual content through a pipeline:

1. **Vision Pipeline** — Image preprocessing (OpenCV), CameraX integration
2. **Detector Registry** — Parallel execution of all registered detectors
3. **Scene Graph Builder** — Unified semantic representation
4. **Renderer Registry** — Parallel rendering to all output modalities

### Multi-Modal Output

Each piece of detected content can be rendered through multiple output channels:

- **Haptics** — VibrationEffect.Composition (API 30+) with Waveform fallback for older devices
- **Spatial Audio** — Oboe low-latency audio with HRTF spatialization
- **Voice** — Sherpa-ONNX offline TTS/STT for spoken descriptions

## What's in v1.0.0

| Module | Purpose |
|---|---|
| `drishti-core` | Plugin interfaces, registry, pipeline, scene graph |
| `drishti-vision` | Image preprocessing, CameraX integration |
| `drishti-graph` | Line charts, scatter plots, bar charts, function plots |
| `drishti-formula` | LaTeX parsing, math evaluation, symbolic computation |
| `drishti-molecule` | SMILES parsing, 3D coordinates, PubChem enrichment |
| `drishti-haptics` | Haptic pattern generation and rendering |
| `drishti-audio` | Spatial audio engine with HRTF |
| `drishti-voice` | Offline TTS/STT with Sherpa-ONNX |
| `drishti-android` | Android HAL, CameraX, device capability detection |

### By the Numbers

- **105 Kotlin files**, 23,285 lines of code
- **665+ unit tests**, 0 failures
- **Full API compatibility validation** (Binary Compatibility Validator)
- **Kotlin Multiplatform** — commonMain + androidMain
- **Apache 2.0 license**

## Getting Started

### Installation

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-core:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-graph:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-haptics:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-audio:1.0.0")
}
```

### Quick Start

```kotlin
import io.drishti.core.Drishti
import io.drishti.graph.GraphPlugin
import io.drishti.haptics.HapticsPlugin
import io.drishti.audio.AudioPlugin

// 1. Initialize with plugins
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .addRenderer(AudioPlugin())
    .build()

// 2. Read any visual content
val diagram = drishti.read(cameraFrame)

// 3. Make it accessible
diagram.haptics()   // Feel the structure
diagram.audio()     // Hear the spatial layout
diagram.voice()     // Get spoken description
```

## Design Principles

1. **Plugin-First** — Every content type is a plugin. Core knows nothing about specific content.
2. **Offline-Only** — No cloud calls. All ML runs on-device.
3. **Developer Experience** — 3-line quick start. Beautiful Kotlin API.
4. **Accessibility Infrastructure** — Not an app. A layer any app integrates.
5. **Modular Distribution** — Each plugin publishes separately. Apps only pull what they need.

## Contributing

Each new diagram type is a separate plugin. Add a detector without modifying core. Every PR should include tests.

See the [GitHub repository](https://github.com/LathissKhumar/DrishtiSTEM) for full contributing guidelines.

## Links

- **GitHub**: https://github.com/LathissKhumar/DrishtiSTEM
- **License**: Apache 2.0
- **Docs**: Architecture guide, plugin development, API reference in `/docs`

---

*Built for developers. Designed for accessibility.*
