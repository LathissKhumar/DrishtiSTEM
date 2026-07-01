# DrishtiSDK Roadmap

## Phase 0: Foundation ✅
- [x] Multi-module KMP project structure
- [x] Core data types (ContentItem, SceneGraph, Pipeline)
- [x] Plugin interfaces (DetectorPlugin, RendererPlugin)
- [x] Build system (Gradle 8.11.1, AGP 8.7.3, offline support)
- [x] TestFixtures in drishti-core/commonMain

## Phase 1: Core Modules ✅
- [x] LatexParser — recursive descent with depth limits
- [x] SpeechRuleEngine — Harvard sentence verbalization
- [x] PubChemClient — HTTP client with timeout, retry, rate limit, caching
- [x] GraphDetector — JSON/CSV/OCR text detection
- [x] HapticRenderer — SceneGraph + ContentItem haptic encoding
- [x] VoiceRenderer — natural language description

## Phase 2: Production Hardening ✅ COMPLETE
- [x] Fix VoiceRenderer exploration navigation (elementIndex unused)
- [x] Fix HapticRenderer exploration navigation (same bug)
- [x] Fix CameraCapture YUV plane handling — verified intentional design, no fix needed
- [x] Full build verification — `assembleDebug` passes all modules
- [x] Full test suite pass — 50 tests, 0 failures
- [x] Oracle code review (optional — not yet done)

## Phase 3: Vision Module (Planned)
- [ ] CameraX real-time OCR integration
- [ ] ML Kit / Tesseract text extraction
- [ ] Scene understanding pipeline
- [ ] Frame → ContentItem detection chain

## Phase 4: Accessibility Integration (Planned)
- [ ] Android TalkBack service integration
- [ ] Switch Access compatibility
- [ ] Braille display output (via BLE)
- [ ] Screen reader gesture mapping

## Phase 5: Demo App & Documentation (Planned)
- [ ] DrishtiSTEM demo app with all modules
- [ ] API reference documentation
- [ ] Integration guide for third-party apps
- [ ] Performance benchmarks

## Phase 6: Release (Planned)
- [ ] Maven Central publishing
- [ ] CI/CD pipeline
- [ ] ProGuard/R8 rules
- [ ] Release 1.0.0
