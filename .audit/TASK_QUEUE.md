# Task Queue — Ordered by Priority

## Active Tasks

(All tasks complete — see Completed section below)

## Completed Tasks

### T-001: Fix VoiceRenderer Exploration Navigation ✅
- Fixed `elementIndex` usage in `renderGraphExploration`, `renderFormulaExploration`, `renderMoleculeExploration`
- NEXT/PREVIOUS now compute correct index from `elementIndex` instead of always returning last/first
- Updated VoicePluginTest assertions to verify correct positional behavior
- **Status**: COMPLETE

### T-002: Fix HapticRenderer Exploration Navigation ✅
- Same fix as VoiceRenderer — `elementIndex` now drives index computation
- Removed 6 dead private methods (`renderNextDataPoint`, `renderPreviousDataPoint`, `renderNextSymbol`, `renderPreviousSymbol`, `renderNextAtom`, `renderPreviousAtom`)
- Replaced with self-contained exploration methods
- **Status**: COMPLETE

### T-003: CameraCapture YUV Handling — Verified OK ✅
- Source audit confirmed Y-plane-only is intentional design choice for OCR/grayscale
- U/V planes not needed for luminance-based text detection
- No fix needed
- **Status**: COMPLETE (no action required)

### T-004: Full Build & Test Verification ✅
- `./gradlew assembleDebug --offline` — BUILD SUCCESSFUL (all modules)
- `./gradlew testDebugUnitTest --offline` — 50 tests, 0 failures
- **Status**: COMPLETE

### T-005: Source Audit — Read All Critical Files ✅
- Read LatexParser.kt, PubChemClient.kt, MoleculeRenderer.kt, HapticRenderer.kt
- Read GraphDetector.kt, VoiceRenderer.kt, SpeechRuleEngine.kt, HapticEncoder.kt, CameraCapture.kt
- Verified 7 of 8 claimed bugs were already fixed or not bugs
- **Status**: COMPLETE

### T-006: TestFixtures Build Fix ✅
- Moved TestFixtures.kt from commonTest to commonMain
- Fixed imports in all 5 modules' test files
- Build verified green
- **Status**: COMPLETE

### T-007: Governance Docs Creation ✅
- Created PROJECT_CONSTITUTION.md, ROADMAP.md, IMPLEMENTATION_PLAN.md
- Created ACTIVE_MILESTONE.md, TASK_QUEUE.md
- **Status**: COMPLETE
