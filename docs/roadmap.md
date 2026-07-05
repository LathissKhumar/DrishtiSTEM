# Roadmap

> Planned evolution of the Drishti SDK from v1.0 to future releases.

---

## Current Release: v1.0.0

**Status:** Released

### What's Included

| Module | Capability | Status |
|:---|:---|:---|
| drishti-core | Plugin registry, pipeline, scene graph | ✅ Complete |
| drishti-vision | OpenCV image preprocessing | ✅ Complete |
| drishti-graph | Graph/chart detection → data extraction | ✅ Complete |
| drishti-formula | Formula OCR → LaTeX parsing | ✅ Complete |
| drishti-molecule | Molecule detection + PubChem enrichment | ✅ Complete |
| drishti-haptics | VibrationEffect.Composition + Waveform fallback | ✅ Complete |
| drishti-audio | Oboe spatial audio + HRTF | ✅ Complete |
| drishti-voice | Sherpa-ONNX offline TTS/STT | ✅ Complete |
| drishti-android | HAL + CameraX integration | ✅ Complete |

### What's Published

- 7 library modules via Maven (mavenLocal)
- JitPack distribution ready
- GitHub Actions CI/CD
- API dump files for binary compatibility

---

## Phase 2: Production Hardening (v1.1)

**Target:** Q3 2026

### Goals

- Production-grade reliability across all modules
- Comprehensive test coverage (>90% per module)
- Performance benchmarking suite
- Memory leak detection
- Thread safety audit

### Planned Work

| Area | Task |
|:---|:---|
| Testing | Integration tests for full pipeline (frame → output) |
| Testing | Concurrency stress tests for ExplorationSession |
| Performance | Benchmark suite for each detector plugin |
| Performance | Memory profiling for long-running sessions |
| Documentation | API reference auto-generation via Dokka |
| Documentation | Integration guides for major Android architectures (MVVM, MVI) |

---

## Phase 3: New Content Types (v1.2)

**Target:** Q4 2026

### Goals

- Expand content type coverage
- Community plugin development
- Plugin marketplace concept

### Planned Plugins

| Plugin | Content Type | Complexity |
|:---|:---|:---|
| Circuit | `CUSTOM("circuit")` | High — schematic parsing |
| Map/Floor Plan | `CUSTOM("map")` | Medium — spatial navigation |
| Flowchart | `CUSTOM("flowchart")` | Medium — node graph extraction |
| Geometry | `CUSTOM("geometry")` | High — construction analysis |
| Table (enhanced) | `TABLE` | Low — OCR + cell extraction |

### Plugin Development Kit

- Template project for new plugins
- Plugin testing harness
- Plugin validation tool
- Documentation generator

---

## Phase 4: Cross-Platform (v2.0)

**Target:** 2027

### Goals

- Expand beyond Android to iOS, Desktop, and Web
- Maintain KMP commonMain for shared logic
- Platform-specific implementations for each target

### Target Platforms

| Platform | Approach |
|:---|:---|
| iOS | Swift bindings via KMP |
| Desktop (JVM) | JavaFX integration |
| WebAssembly | Wasm target for browser-based accessibility |
| Raspberry Pi | Linux ARM64 build |

### Architecture Changes

- Platform abstraction layer formalization
- expect/actual pattern for platform-specific code
- Native library packaging for each platform

---

## Phase 5: AI Model Integration (v2.1)

**Target:** 2027

### Goals

- Support for on-device ML models
- Model provider abstraction
- Runtime model switching

### Planned Providers

| Provider | Model Type | Use Case |
|:---|:---|:---|
| YOLO | Object detection | Shape/object detection |
| SAM | Segmentation | Precise content extraction |
| PaddleOCR | OCR | Text extraction from images |
| Whisper | Speech | Voice input |
| Gemma | LLM | Natural language descriptions |
| Pix2Tex | Math | LaTeX formula recognition |

### Provider Interface

```kotlin
interface VisionProvider {
    suspend fun detect(image: Frame): List<Detection>
}

interface OCRProvider {
    suspend fun extractText(image: Frame): List<TextBlock>
}

interface LLMProvider {
    suspend fun describe(content: ContentItem): String
}
```

---

## Phase 6: Advanced Features (v3.0)

**Target:** 2028

### Goals

- Multi-modal AI capabilities
- Real-time collaboration
- Cloud synchronization (optional)
- Custom hardware support

### Planned Features

| Feature | Description |
|:---|:---|
| Agent System | Autonomous agents for vision, OCR, scene understanding |
| Event Bus | Decoupled agent communication |
| Memory System | Retrieval-augmented generation |
| Knowledge Graph | Semantic relationship mapping |
| Embedding Engine | Vector similarity search |
| Teacher Dashboard | Real-time monitoring for educators |
| Braille Device | Hardware integration for refreshable braille displays |
| AR Overlay | Augmented reality accessibility layer |

---

## Long-Term Vision

### 5-Year Goals

1. **Universal Accessibility Infrastructure** — Used by every major accessibility platform
2. **Plugin Ecosystem** — 50+ content type plugins maintained by community
3. **Standard Protocol** — De facto standard for visual-to-multimodal conversion
4. **Research Platform** — Foundation for accessibility AI research
5. **Global Impact** — Serving millions of visually impaired users worldwide

### Success Metrics

| Metric | Target |
|:---|:---|
| GitHub Stars | 10,000+ |
| Community Plugins | 50+ |
| Monthly Active Users | 100,000+ |
| Platform Coverage | Android, iOS, Web, Desktop |
| Content Types | 20+ supported |
| Response Time | < 500ms end-to-end |

---

## Contributing to the Roadmap

Community input drives our priorities:

1. **GitHub Issues** — Request features, report bugs
2. **Discussions** — Propose architectural changes
3. **Pull Requests** — Implement new plugins
4. **RFC Process** — Major changes go through RFC

We follow YAGNI (You Aren't Gonna Need It) ruthlessly. Features are added only when there's real user demand, not speculative future need.

---

## Versioning Policy

| Version | Breaking Changes | New Features | Bug Fixes |
|:---|:---|:---|:---|
| Major (x.0.0) | Yes | Yes | Yes |
| Minor (0.x.0) | No | Yes | Yes |
| Patch (0.0.x) | No | No | Yes |

- **v1.0.0** → Initial release (current)
- **v1.1.x** → Production hardening
- **v1.2.x** → New content types
- **v2.0.0** → Cross-platform (breaking API changes expected)
- **v2.1.x** → AI model integration
- **v3.0.0** → Advanced features (breaking changes)
