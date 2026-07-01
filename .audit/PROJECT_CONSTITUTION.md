# DrishtiSTEM Project Constitution

## Vision
Production-grade open-source STEM accessibility SDK that transforms visual STEM content into haptic, audio, and voice modalities for blind and visually impaired learners.

## Core Principles

### 1. Production Quality Over Speed
- Every line of code must be indistinguishable from senior human-engineered OSS
- No placeholders, no TODO markers, no demo/mock data in production modules
- All edge cases handled. All nulls checked. All division guarded. All bounds validated.

### 2. SDK-First Architecture
- Plugin-based module system with Clean Architecture layers
- Each module is independently compilable, testable, and publishable
- Fluent public API: `Drishti.load(...).analyze().toHaptics()`
- SOLID principles enforced across all module boundaries

### 3. No AI Artifacts
- Zero traces of AI generation: no generic variable names, no over-commenting, no pattern repetition
- Code style matches established Android/KMP OSS projects (Ktor, kotlinx.serialization, Coil)
- Every public API has KDoc. Internal implementation does not.

### 4. Offline-First, TDD Mandatory
- All modules compile and test offline (`--offline` flag)
- Test-driven development: RED → GREEN → REFACTOR enforced
- Integration tests use real data structures, not mocks

### 5. KMP Multiplatform
- Common business logic in `commonMain`
- Platform-specific implementations in `androidMain` / `iosMain` / `jvmMain`
- No expect/actual hacks for shared logic — proper abstraction layers

## Module Contracts

| Module | Responsibility | Public API Surface |
|--------|---------------|-------------------|
| `drishti-core` | Data types, interfaces, contracts | ContentItem, SceneGraph, Pipeline, DetectorPlugin |
| `drishti-formula` | LaTeX → AST → speech/haptic | LatexParser, SpeechRuleEngine, FormulaRenderer |
| `drishti-molecule` | Molecule data + rendering | PubChemClient, MoleculeRenderer, MoleculePlugin |
| `drishti-graph` | Graph detection + rendering | GraphDetector, GraphRenderer, DataExtractor |
| `drishti-audio` | Sonification engine | AudioRenderer, SpatialMapper, AudioPlugin |
| `drishti-haptics` | Haptic encoding + output | HapticRenderer, HapticEncoder, HapticsPlugin |
| `drishti-voice` | Text-to-speech verbalization | VoiceRenderer, FormulaSpeech, VoicePlugin |
| `drishti-vision` | Camera + OCR pipeline | (planned, not yet implemented) |
| `drishti-android` | Android platform integration | CameraCapture, AndroidSensors, AndroidHaptics |

## Non-Negotiables
- Apache-2.0 license on all files
- Min SDK: API 30 (Android 11)
- Gradle 8.11.1 / AGP 8.7.3
- Convention commits: `feat(scope): description`, `fix(scope): description`
- Build must pass: `./gradlew assembleDebug --offline`
- No runtime crashes from null/division/bounds errors
