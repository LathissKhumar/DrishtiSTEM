# Implementation Plan — Current Module Status

## Build Status: 🟢 GREEN
Last verified: `./gradlew assembleDebug --offline` — all modules compile.

## Module Status

### drishti-core ✅
- ContentItem hierarchy (GraphContent, FormulaContent, MoleculeContent, etc.)
- SceneGraph with adjacencyIndex, bounds, nodes, edges
- Pipeline orchestration
- TestFixtures (shared test data builders)
- **Status**: Production-ready

### drishti-formula ✅
- LatexParser: recursive descent, depth-limited, 675 lines
- SpeechRuleEngine: Harvard sentence verbalization, 314 lines
- FormulaRenderer: AST → haptic/audio/voice
- **Status**: Production-ready

### drishti-molecule ✅
- PubChemClient: 30s timeout, 3 retries, rate limiting, coalescing, caching
- MoleculeRenderer: haptic/audio/voice with weight-scaled intensities
- **Status**: Production-ready

### drishti-graph ✅
- GraphDetector: JSON/CSV/OCR text → GraphContent
- DataExtractor: multi-format data parsing
- GraphRenderer: graph → haptic/audio/voice
- **Status**: Production-ready

### drishti-audio ✅
- AudioRenderer: sonification engine
- SpatialMapper: 3D audio positioning
- **Status**: Production-ready

### drishti-haptics ⚠️
- HapticRenderer: SceneGraph + ContentItem rendering
- HapticEncoder: pulse → VibrationEffect encoding
- **Issue**: Exploration navigation is stateless (NEXT always last, PREVIOUS always first)
- **Status**: Fix needed

### drishti-voice ⚠️
- VoiceRenderer: natural language descriptions
- FormulaSpeech: MathCAT verbalization
- **Issue**: Same exploration navigation bug as HapticRenderer
- **Status**: Fix needed

### drishti-android ⚠️
- CameraCapture: CameraX integration
- **Issue**: Only reads Y plane from YUV_420_888
- **Status**: Minor fix needed

### drishti-vision ❌
- **Status**: Not implemented (directory missing)

### demo-app 📋
- **Status**: Integration shell only

## Priority Fixes (This Sprint)
1. VoiceRenderer exploration navigation
2. HapticRenderer exploration navigation
3. CameraCapture YUV handling
4. Full build + test verification
