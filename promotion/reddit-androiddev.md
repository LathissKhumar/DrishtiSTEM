# DrishtiSDK — Reddit r/androiddev Post

## Title

[D] Drishti SDK v1.0.0 — Open-source accessibility infrastructure for STEM content (haptics, spatial audio, voice) | Kotlin Multiplatform

## Post

Hey r/androiddev,

I just released v1.0.0 of Drishti SDK — a Kotlin Multiplatform library that makes visual STEM content accessible to blind and visually impaired users.

**The problem:** Most screen readers work with text. But STEM content (graphs, formulas, molecular structures) is inherently visual. A scatter plot doesn't "read" well with a screen reader.

**The solution:** Drishti provides a plugin-based detection → rendering pipeline:

1. **Detect** — Graph, Formula, or Molecule detector plugins identify content in camera frames or images
2. **Represent** — Content is converted to a semantic scene graph
3. **Render** — Multiple output modalities make the content accessible:
   - 📳 **Haptics** — Vibration patterns that convey spatial structure
   - 🔊 **Spatial Audio** — HRTF-based audio that maps position to sound
   - 🗣️ **Voice** — Spoken descriptions of content and relationships

**Architecture highlights:**
- Plugin-based — add new content types without touching core
- Fully offline — all ML on-device (no cloud calls)
- Modular — pull only the plugins you need
- 665+ unit tests, full API compatibility validation

**Quick start:**
```kotlin
implementation("com.github.lathiss.DrishtiSTEM:drishti-core:1.0.0")
implementation("com.github.lathiss.DrishtiSTEM:drishti-graph:1.0.0")
implementation("com.github.lathiss.DrishtiSTEM:drishti-haptics:1.0.0")
```

**Links:**
- GitHub: https://github.com/lathiss/DrishtiSTEM
- Docs: See README for architecture guide, plugin development, API reference
- License: Apache 2.0

Looking for feedback — especially from anyone working on accessibility, KMP, or plugin architectures. Contributions welcome!

---

**Flair:** [Library/Resource]
