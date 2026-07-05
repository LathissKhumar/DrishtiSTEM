# DrishtiSDK — Show HN Post

## Title

Drishti SDK – Open-source accessibility infrastructure for visual STEM content (haptics, spatial audio, voice)

## Post

I've been building an SDK that converts visual STEM content — graphs, formulas, molecules, diagrams — into haptic feedback, spatial audio, and voice guidance for blind and visually impaired users.

**Why this exists:** Most accessibility tools work with text. STEM content is inherently visual — a scatter plot, a chemical bond, a handwritten integral. Drishti treats each content type as a plugin in a detection→rendering pipeline, so any app can make any diagram accessible without AI expertise.

**How it works:**
- Plugin-based architecture — each diagram type (graph, formula, molecule) is a separate detector plugin
- Fully offline — all ML runs on-device (LiteRT, ONNX Runtime, Sherpa-ONNX)
- Multiple output modalities — haptic vibration patterns, spatial audio with HRTF, spoken voice descriptions
- Kotlin Multiplatform (KMP) — commonMain + androidMain

**What's in v1.0.0:**
- 9 modules (core, vision, graph, formula, molecule, haptics, audio, voice, android)
- 665+ unit tests, 23K LOC
- Full API compatibility validation (Binary Compatibility Validator)
- JitPack distribution — one line to add to any Android project

```kotlin
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .addRenderer(AudioPlugin())
    .build()

val diagram = drishti.read(cameraFrame)
diagram.haptics()  // Feel the structure
diagram.audio()    // Hear the spatial layout
```

GitHub: https://github.com/lathiss/DrishtiSTEM

Would love feedback on the plugin architecture — especially from folks working on accessibility, assistive tech, or Kotlin Multiplatform.

---

**Tags:** showhn, accessibility, kotlin, android, stem, open-source
