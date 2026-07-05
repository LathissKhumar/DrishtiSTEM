# DrishtiSDK — Twitter/X Launch Thread

## Thread

**1/7** 🧵
I just released Drishti SDK v1.0.0 — open-source accessibility infrastructure for visual STEM content.

Graphs, formulas, molecules → haptic feedback, spatial audio, voice guidance.

For blind and visually impaired users who need non-visual access to STEM content.

👇 Thread

**2/7**
The problem: STEM content is inherently visual.

A scatter plot. A chemical bond. A handwritten integral.

Screen readers can't convey spatial relationships. A bar chart isn't just "bar chart" — it's heights, positions, trends, intersections.

**3/7**
How Drishti works:

Input → Vision Pipeline → Detector Plugins → Scene Graph → Renderer Plugins → Output

Each content type is a plugin. Each output modality is a plugin. Mix and match.

**4/7**
What's in v1.0.0:
• 9 modules (core, vision, graph, formula, molecule, haptics, audio, voice, android)
• 665+ unit tests
• Full API compatibility validation
• Kotlin Multiplatform (KMP)
• Apache 2.0 license

**5/7**
The plugin architecture:

```
Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .addRenderer(AudioPlugin())
    .build()
```

3 lines to make any diagram accessible. No AI expertise required.

**6/7**
Key design decisions:

✅ Plugin-first — core knows nothing about specific content types
✅ Offline-only — all ML on-device, no cloud calls
✅ Modular — pull only what you need
✅ Production-grade — no mocks, no placeholders, no TODOs

**7/7**
Built for developers. Designed for accessibility.

GitHub: https://github.com/LathissKhumar/DrishtiSTEM

Star ⭐ if this resonates. PRs welcome.

Accessibility is infrastructure, not a feature.

#Accessibility #Kotlin #Android #OpenSource #STEM #A11y
