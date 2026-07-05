<div align="center">

# 🔭 Drishti SDK

### The Accessibility Infrastructure for Visual STEM Content

**Convert any visual content — graphs, formulas, molecules, diagrams, circuits, maps — into haptic feedback, spatial audio, and voice guidance. Plugin-based. Fully offline. Developer-first.**

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=for-the-badge)](https://opensource.org/licenses/Apache-2.0)
[![API Level](https://img.shields.io/badge/API-30%2B-brightgreen?style=for-the-badge)](https://developer.android.com/about/versions/11)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-purple?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge)](https://github.com/LathissKhumar/DrishtiSDK/actions/workflows/ci.yml)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-orange?style=for-the-badge)](CONTRIBUTING.md)
[![GitHub stars](https://img.shields.io/github/stars/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/network/members)
[![GitHub issues](https://img.shields.io/github/issues/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/pulls)
[![GitHub last commit](https://img.shields.io/github/last-commit/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/commits/main)
[![GitHub license](https://img.shields.io/github/license/LathissKhumar/DrishtiSTEM?style=for-the-badge&logo=github)](https://github.com/LathissKhumar/DrishtiSTEM/blob/main/LICENSE)

---

**[Install](#-install)** · **[Quick Start](#-quick-start-3-lines)** · **[Architecture](#-architecture)** · **[Plugins](#-plugins)** · **[Examples](#-examples)** · **[Roadmap](#-roadmap)** · **[Contributing](#-contributing)**

---

</div>

## 💡 What is Drishti SDK?

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

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      drishti-core                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Detector     │  │ Renderer     │  │ Pipeline         │   │
│  │ Registry     │  │ Registry     │  │ Orchestrator     │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         ↑                    ↑                    ↑
    ┌────┴────┐        ┌─────┴─────┐        ┌─────┴─────┐
    │         │        │           │        │           │
┌───┴──┐ ┌───┴──┐ ┌────┴────┐ ┌────┴───┐ ┌───┴───┐ ┌────┴────┐
│Graph │ │Formula│ │Molecule│ │Circuit │ │Map    │ │ ...     │
│Plugin│ │Plugin │ │Plugin  │ │Plugin  │ │Plugin │ │ Plugins │
└──────┘ └───────┘ └────────┘ └────────┘ └───────┘ └─────────┘
```

**Every content type is a plugin.** No hardcoded modules. Contributors add support for new diagram types without touching core.

---

## ✨ Features

| Category | Feature | Status |
|:---|:---|:---|
| **Core** | Plugin registry & discovery | ✅ Complete |
| **Core** | Pipeline orchestrator (parallel detector execution) | ✅ Complete |
| **Core** | Scene graph & semantic representation | ✅ Complete |
| **Vision** | OpenCV image preprocessing pipeline | ✅ Complete |
| **Vision** | CameraX integration for live camera | 🚧 In Progress |
| **Graph** | Graph detection → coordinate extraction → sonification | ✅ Complete |
| **Formula** | Formula OCR → LaTeX → math understanding | ✅ Complete |
| **Molecule** | SMILES parsing → 3D structure → PubChem enrichment | ✅ Complete |
| **Haptics** | VibrationEffect.Composition + Waveform fallback | ✅ Complete |
| **Audio** | Oboe low-latency spatial audio + HRTF | ✅ Complete |
| **Voice** | Sherpa-ONNX offline TTS/STT | ✅ Complete |
| **Android** | HAL for device capability detection | ✅ Complete |

---

## 📦 Install

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
    // Core SDK
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-core:1.0.0")
    
    // Plugins (pick what you need)
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-graph:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-formula:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-molecule:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-circuit:1.0.0") // future
    
    // Renderers
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-haptics:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-audio:1.0.0")
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-voice:1.0.0")
    
    // Android integration
    implementation("com.github.LathissKhumar.DrishtiSTEM:drishti-android:1.0.0")
}
```

---

## 🚀 Quick Start (3 Lines)

```kotlin
import io.drishti.core.Drishti
import io.drishti.graph.GraphPlugin
import io.drishti.formula.FormulaPlugin
import io.drishti.haptics.HapticsPlugin
import io.drishti.audio.AudioPlugin

// 1. Initialize with plugins
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addDetector(FormulaPlugin())
    .addRenderer(HapticsPlugin())
    .addRenderer(AudioPlugin())
    .build()

// 2. Read any visual content
val diagram = drishti.read(cameraFrame)  // or bitmap, or image file

// 3. Make it accessible
diagram.haptics()   // Feel the structure
diagram.audio()     // Hear the spatial layout  
diagram.voice()     // Get spoken description
diagram.explore()   // Interactive exploration mode
```

That's it. No AI expertise required.

---

## 🧩 Plugins

| Plugin | Package | Detects | Output |
|:---|:---|:---|:---|
| **Graph** | `drishti-graph` | Line charts, scatter plots, bar charts, function plots | Axes, data points, trends, intersections |
| **Formula** | `drishti-formula` | LaTeX formulas, handwritten math, printed equations | Parsed AST, LaTeX string, evaluated values |
| **Molecule** | `drishti-molecule` | Chemical structures, bond diagrams, SMILES | Atom/bond graph, 3D coordinates, PubChem data |
| **Circuit** | `drishti-circuit` (planned) | Circuit diagrams, schematics, logic gates | Netlist, component graph, simulation |
| **Map** | `drishti-map` (planned) | Maps, floor plans, GIS diagrams | Spatial graph, navigation hints |
| **Flowchart** | `drishti-flowchart` (planned) | Flowcharts, decision trees, process diagrams | Node graph, execution paths |
| **Geometry** | `drishti-geometry` (planned) | Geometric constructions, proofs, diagrams | Theorem steps, measurements |

**Write your own plugin:**

```kotlin
class MyCustomPlugin : DetectorPlugin {
    override val contentType = ContentType.CUSTOM("my-type")
    
    override fun detect(frame: Frame): List<ContentItem> {
        // Your detection logic
        return listOf(ContentItem(...))
    }
}

// Register
Drishti.Builder().addDetector(MyCustomPlugin()).build()
```

---

## 🏗️ Architecture Deep Dive

```
Input (Camera/Bitmap/File)
        │
        ▼
┌───────────────────┐
│  Vision Pipeline  │  ← drishti-vision (OpenCV, CameraX, preprocessing)
│  (shared across   │
│   all plugins)    │
└─────────┬─────────┘
          │ Frame + Features
          ▼
┌───────────────────┐
│  Detector Registry│  ← Parallel execution of all registered detectors
│  (Graph, Formula, │
│   Molecule, ...)  │
└─────────┬─────────┘
          │ ContentItems
          ▼
┌───────────────────┐
│  Scene Graph      │  ← Unified semantic representation
│  Builder          │
└─────────┬─────────┘
          │ SceneGraph
          ▼
┌───────────────────┐
│  Renderer Registry│  ← Parallel rendering to all outputs
│  (Haptics, Audio, │
│   Voice, Text)    │
└─────────┬─────────┘
          │ MultimodalOutput
          ▼
┌───────────────────┐
│  Drishti Diagram  │  ← Beautiful API: .haptics() .audio() .voice() .explore()
└───────────────────┘
```

---

## 🎯 Design Principles

1. **Plugin-First** — Every content type is a plugin. Core knows nothing about graphs, formulas, or molecules.
2. **Offline-Only** — No cloud calls. All ML runs on-device (LiteRT, ONNX, Sherpa-ONNX).
3. **Developer Experience** — 3-line quick start. Beautiful Kotlin API. Comprehensive docs.
4. **Accessibility Infrastructure** — Not a blind-assistance app. A layer any app integrates.
5. **Modular Distribution** — Each plugin publishes separately. Apps only pull what they need.
6. **Real Implementations** — No mocks, no placeholders, no TODOs. Production-grade.

---

## 📁 Repository Structure

```
DrishtiSTEM/
├── drishti-core/          # Plugin interfaces, registry, pipeline, scene graph
├── drishti-vision/        # Shared vision pipeline (OpenCV, CameraX, preprocessing)
├── drishti-graph/         # Graph detection plugin
├── drishti-formula/       # Formula OCR plugin
├── drishti-molecule/      # Molecule detection + PubChem plugin
├── drishti-haptics/       # Haptic rendering engine
├── drishti-audio/         # Spatial audio engine (Oboe)
├── drishti-voice/         # Voice assistant (Sherpa-ONNX)
├── drishti-android/       # Android HAL + CameraX integration
├── drishti-demo/          # DrishtiSTEM demo app (showcase)
└── examples/              # Integration examples
```

Each plugin is independently versioned and published to JitPack.

---

## 🛠️ Tech Stack

| Layer | Technology |
|:---|:---|
| **Language** | Kotlin 2.1 + C++ (NDK for audio) |
| **Build** | Gradle 8.11 + KMP (commonMain + androidMain) |
| **Vision** | OpenCV 4.13 + CameraX 1.5 |
| **ML** | LiteRT + ONNX Runtime + Qualcomm QNN (NPU) |
| **Math** | mXparser (expressions) + Symja CAS (symbolic) |
| **Chemistry** | OpenChemLib (SMILES) + PubChem/NFDI4Chem/NCI APIs |
| **Haptics** | VibrationEffect.Composition (API 30+) + Waveform fallback |
| **Audio** | Oboe 1.9.3 + Android Spatializer (API 32+) |
| **Voice** | Sherpa-ONNX (offline STT/TTS) |

---

## 🗺️ Roadmap

| Phase | Milestone | Target |
|:---|:---|:---|
| **1** | Core + Vision + Graph Plugin + Haptics + Audio | ✅ Complete |
| **2** | Formula Plugin + Molecule Plugin + Voice | ✅ Complete |
| **3** | Android HAL + CameraX + Demo App | 🚧 In Progress |
| **4** | Circuit Plugin + Map Plugin + Flutter/React Native bindings | 📋 Planned |
| **5** | WebAssembly port + Python bindings | 📋 Planned |
| **6** | Plugin marketplace + Auto-discovery | 📋 Planned |

---

## 🤝 Contributing

We make it easy to contribute:

1. **Good First Issues** — Each new diagram type is a separate plugin
2. **No Core Touches** — Add a detector without modifying core
3. **Visual Feedback** — Every PR includes a GIF of the plugin working

```bash
# Get started
git clone https://github.com/LathissKhumar/DrishtiSTEM
cd DrishtiSTEM
./gradlew build
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📖 Documentation

- [Architecture Guide](docs/architecture.md)
- [Plugin Development](docs/plugin-development.md)
- [API Reference](docs/api-reference.md)
- [Android Integration](docs/android-integration.md)
- [Building from Source](docs/building.md)

---

## 🏆 Benchmarks

| Operation | Target | Current |
|:---|:---|:---|
| Frame preprocessing | < 30ms | ✅ |
| Graph detection | < 50ms | ✅ |
| Formula OCR | < 100ms | ✅ |
| Molecule recognition | < 150ms | ✅ |
| Haptic render | < 20ms | ✅ |
| Audio spatialization | < 50ms | ✅ |
| Voice response | < 200ms | ✅ |
| **End-to-end** | **< 300ms** | ✅ |

---

## 📄 License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

Copyright 2026 Drishti SDK Contributors.

---

## 🙏 Acknowledgments

- [OpenCV](https://opencv.org/) — Computer vision foundation
- [Oboe](https://github.com/google/oboe) — Low-latency audio
- [Sherpa-ONNX](https://github.com/k2-fsa/sherpa-onnx) — Offline STT/TTS
- [OpenChemLib](https://github.com/openchemlib/openchemlib) — Chemistry toolkit
- [mXparser](https://github.com/mariuszgromada/MathParser.org-mXparser) — Math expressions
- [Symja](https://github.com/axkr/symja_android_library) — Computer algebra

---

<div align="center">

**Built for developers. Designed for accessibility.**

[⬆ Back to Top](#-drishti-sdk)

</div>
