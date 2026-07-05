# Kotlin Weekly / Android Weekly Submission

## Subject

Drishti SDK v1.0.0 — Open-source accessibility infrastructure for visual STEM content (Kotlin Multiplatform)

## Body

Hi,

I'd like to submit Drishti SDK for inclusion in the newsletter.

**Drishti SDK** is an open-source Kotlin Multiplatform library that converts visual STEM content — graphs, formulas, molecules — into haptic feedback, spatial audio, and voice guidance for blind and visually impaired users.

**Key highlights:**
- Plugin-based architecture: each content type (graph, formula, molecule) is a separate detector plugin
- Fully offline: all ML runs on-device (no cloud calls)
- Multiple output modalities: haptics (VibrationEffect.Composition), spatial audio (Oboe + HRTF), voice (Sherpa-ONNX TTS/STT)
- 665+ unit tests, full API compatibility validation (BCV)
- Kotlin Multiplatform (KMP) with commonMain + androidMain
- Apache 2.0 license

**Quick start (3 lines):**
```kotlin
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .addRenderer(AudioPlugin())
    .build()
```

**Links:**
- GitHub: https://github.com/LathissKhumar/DrishtiSTEM
- License: Apache 2.0

Thank you for your consideration.
