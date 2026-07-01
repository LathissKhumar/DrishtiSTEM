# Contributing to Drishti SDK

Thank you for your interest in making visual content accessible! This guide will help you contribute effectively.

---

## 🎯 Ways to Contribute

| Type | Difficulty | Example |
|:---|:---|:---|
| **New Plugin** | Medium | Add Sankey diagram, Pie chart, Circuit support |
| **Bug Fix** | Easy-Medium | Fix graph detection edge case, haptic timing |
| **Documentation** | Easy | Improve API docs, add examples, fix typos |
| **Testing** | Easy-Medium | Add test cases, improve coverage |
| **Performance** | Hard | Optimize pipeline latency, reduce memory |
| **New Renderer** | Hard | Add Braille display output, refreshable tactile |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 30+ (API 30)
- NDK 28+ (for audio C++ components)
- Git

### Setup

```bash
# 1. Fork the repository
# Go to https://github.com/lathiss/DrishtiSTEM → Click "Fork"

# 2. Clone your fork
git clone https://github.com/YOUR_USERNAME/DrishtiSTEM.git
cd DrishtiSTEM

# 3. Add upstream remote
git remote add upstream https://github.com/LathissKhumar/DrishtiSTEM.git

# 4. Create a branch
git checkout -b feature/my-new-plugin

# 5. Build the project
./gradlew build
```

---

## 🧩 Writing a New Plugin

This is the most common contribution type. Each content type is a separate plugin.

### Step 1: Scaffold Your Plugin

```bash
# From project root
./gradlew :drishti-core:createPlugin --name=SankeyChart
```

This creates:

```
drishti-sankey/
├── build.gradle.kts
├── src/
│   ├── commonMain/kotlin/io/drishti/plugins/sankey/
│   │   ├── SankeyDetector.kt
│   │   ├── SankeyContent.kt
│   │   └── SankeyRenderer.kt
│   ├── androidMain/kotlin/io/drishti/plugins/sankey/
│   └── commonTest/kotlin/io/drishti/plugins/sankey/
│       └── SankeyDetectorTest.kt
```

### Step 2: Implement the Detector

```kotlin
package io.drishti.plugins.sankey

import io.drishti.core.DetectorPlugin
import io.drishti.core.ContentType
import io.drishti.core.Frame
import io.drishti.core.ContentItem

class SankeyDetector : DetectorPlugin {
    
    override val contentType = ContentType.CHART("sankey")
    
    override fun detect(frame: Frame): List<ContentItem> {
        // 1. Analyze image for Sankey-specific features
        // 2. Extract nodes, flows, values
        // 3. Return structured content
        
        return listOf(
            SankeyContent(
                nodes = extractNodes(frame),
                flows = extractFlows(frame),
                values = extractValues(frame)
            )
        )
    }
    
    private fun extractNodes(frame: Frame): List<SankeyNode> {
        // Implementation using OpenCV or ML inference
        TODO("Implement node extraction")
    }
    
    private fun extractFlows(frame: Frame): List<SankeyFlow> {
        TODO("Implement flow extraction")
    }
    
    private fun extractValues(frame: Frame): Map<String, Double> {
        TODO("Implement value extraction")
    }
}
```

### Step 3: Implement the Renderer

```kotlin
package io.drishti.plugins.sankey

import io.drishti.core.RendererPlugin
import io.drishti.core.ContentItem
import io.drishti.core.HapticOutput
import io.drishti.core.AudioOutput

class SankeyRenderer : RendererPlugin {
    
    override fun renderHaptic(content: ContentItem): HapticOutput {
        val sankey = content as SankeyContent
        
        // Create haptic pattern:
        // - Each node = tactile pulse
        // - Flow width = vibration intensity
        // - Direction = vibration rhythm
        
        return HapticOutput(
            pattern = sankey.flows.map { flow ->
                HapticPulse(
                    intensity = flow.value.toFloat() / maxValue,
                    duration = flow.widthMs,
                    position = flow.sourcePosition
                )
            }
        )
    }
    
    override fun renderAudio(content: ContentItem): AudioOutput {
        val sankey = content as SankeyContent
        
        // Create spatial audio:
        // - Node positions = audio source positions
        // - Flow volume = flow value
        // - Direction = stereo panning
        
        return AudioOutput(
            sources = sankey.flows.map { flow ->
                AudioSource(
                    position = flow.sourcePosition,
                    volume = flow.value / maxValue,
                    label = "${flow.source} → ${flow.target}"
                )
            }
        )
    }
}
```

### Step 4: Write Tests

```kotlin
package io.drishti.plugins.sankey

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SankeyDetectorTest {
    
    @Test
    fun `detect extracts sankey nodes correctly`() {
        // Given
        val detector = SankeyDetector()
        val frame = TestFrames.createSankeyFrame()
        
        // When
        val result = detector.detect(frame)
        
        // Then
        assertEquals(1, result.size)
        val content = result[0] as SankeyContent
        assertEquals(4, content.nodes.size)
        assertEquals(3, content.flows.size)
    }
    
    @Test
    fun `detect handles empty image`() {
        // Given
        val detector = SankeyDetector()
        val frame = TestFrames.createEmptyFrame()
        
        // When
        val result = detector.detect(frame)
        
        // Then
        assertEquals(0, result.size)
    }
}
```

### Step 5: Submit PR

```bash
# Commit with conventional format
git add .
git commit -m "feat(sankey): add Sankey chart detection plugin"

# Push to your fork
git push origin feature/my-new-plugin

# Create PR on GitHub
```

---

## 📝 Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|:---|:---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `test` | Adding tests |
| `refactor` | Code refactoring |
| `perf` | Performance improvement |
| `chore` | Build/config changes |

### Scopes

| Scope | Description |
|:---|:---|
| `core` | drishti-core module |
| `vision` | drishti-vision module |
| `graph` | drishti-graph plugin |
| `formula` | drishti-formula plugin |
| `molecule` | drishti-molecule plugin |
| `haptics` | drishti-haptics module |
| `audio` | drishti-audio module |
| `voice` | drishti-voice module |
| `android` | drishti-android module |
| `demo` | drishti-demo app |

### Examples

```bash
feat(graph): add support for scatter plots
fix(haptics): correct timing for rapid sequences
docs(api): add plugin development guide
test(formula): add LaTeX parsing edge cases
perf(vision): optimize image preprocessing pipeline
```

---

## 🐛 Reporting Bugs

### Bug Report Template

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Load image with '...'
2. Run detection with '...'
3. See error

**Expected behavior**
What you expected to happen.

**Screenshots/Videos**
If applicable, add visual evidence.

**Environment**
- Device: [e.g., Pixel 7]
- OS: [e.g., Android 14]
- SDK Version: [e.g., 1.0.0]

**Additional context**
Any other information.
```

---

## 💡 Suggesting Features

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
A clear description of the problem. Ex. "I'm always frustrated when..."

**Describe the solution you'd like**
What you want to happen.

**Describe alternatives you've considered**
Other solutions you've thought about.

**Additional context**
Mockups, examples, or references.
```

---

## 🧪 Testing Guidelines

### Test Types

| Type | Location | Command |
|:---|:---|:---|
| **Unit Tests** | `src/commonTest/` | `./gradlew :module:test` |
| **Android Tests** | `src/androidUnitTest/` | `./gradlew :module:testDebugUnitTest` |
| **Instrumented Tests** | `src/androidTest/` | `./gradlew :module:connectedAndroidTest` |

### Test Requirements

- All new features MUST have tests
- Bug fixes MUST include a regression test
- Maintain or improve code coverage
- Tests must pass before merge

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :drishti-graph:test

# Android instrumented tests
./gradlew :drishti-android:connectedAndroidTest
```

---

## 📋 Code Style

### Kotlin

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names
- Keep functions small and focused
- Add KDoc for public APIs

### Example

```kotlin
/**
 * Detects graph content in an image frame.
 *
 * This detector identifies line charts, scatter plots,
 * and bar charts, extracting coordinate data and metadata.
 *
 * @param frame The input image frame to analyze
 * @return List of detected graph content items
 * @throws IllegalArgumentException if frame is empty
 */
override fun detect(frame: Frame): List<ContentItem> {
    require(frame.isNotEmpty()) { "Frame cannot be empty" }
    
    // Implementation
}
```

---

## 🎨 Pull Request Checklist

Before submitting your PR:

- [ ] Code follows project style guidelines
- [ ] Tests added/updated
- [ ] All tests pass locally
- [ ] Documentation updated (if applicable)
- [ ] Commit messages follow convention
- [ ] PR description is clear
- [ ] Screenshots/GIFs included (for visual changes)
- [ ] No breaking changes (or clearly documented)

### PR Description Template

```markdown
## Description
Brief description of changes.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## How Has This Been Tested?
Describe tests run.

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code
- [ ] I have updated documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing unit tests pass locally
```

---

## 🏷️ Issue Labels

| Label | Description |
|:---|:---|
| `good-first-issue` | Perfect for newcomers |
| `plugin-request` | New plugin suggestion |
| `bug` | Something isn't working |
| `enhancement` | New feature or improvement |
| `documentation` | Docs need work |
| `help-wanted` | Extra attention needed |
| `priority-critical` | Must fix immediately |
| `priority-high` | Should fix soon |
| `priority-medium` | Normal priority |
| `priority-low` | Nice to have |

---

## 🎁 Recognition

Contributors get:

- **GitHub Profile** — Your name in the contributors list
- **Release Notes** — Mentioned in changelogs
- **Swag** — Stickers for significant contributions (when available)
- **Maintainer Access** — For sustained contributors

---

## 📞 Getting Help

- **GitHub Discussions** — Ask questions, share ideas
- **Discord** — Real-time chat (link in repo)
- **Issues** — Bug reports, feature requests
- **Twitter** — Follow @DrishtiSDK for updates

---

## 📜 Code of Conduct

We follow the [Contributor Covenant](https://www.contributor-covenant.org/):

- **Be respectful** — Treat everyone with respect
- **Be constructive** — Provide helpful feedback
- **Be inclusive** — Welcome diverse perspectives
- **Be patient** — Help newcomers learn

---

## 🙏 Thank You

Every contribution matters. Whether it's:

- A one-line typo fix
- A new plugin for a diagram type
- Performance optimization
- Documentation improvement

You're helping make visual content accessible to everyone.

**Welcome to the Drishti SDK community!** 🔭
