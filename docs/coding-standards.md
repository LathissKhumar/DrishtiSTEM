# Coding Standards

> Naming conventions, SOLID principles, Kotlin style guide, and quality gates for the Drishti SDK codebase.

---

## Kotlin Language Rules

### Visibility Modifiers

Every public declaration MUST have an explicit visibility modifier.

```kotlin
// CORRECT
public class Pipeline(private val config: PipelineConfig)
public fun detect(frame: Frame): List<ContentItem>
private val lock = Lock()

// WRONG — implicit public
class Pipeline(val config: PipelineConfig)
fun detect(frame: Frame): List<ContentItem>
```

### Return Types

Every public function MUST have an explicit return type.

```kotlin
// CORRECT
public fun detect(frame: Frame): List<ContentItem> { ... }

// WRONG
public fun detect(frame: Frame) { ... }  // missing return type
```

### No Bang Operators

Never use `!!`. Use `require()`, `check()`, or nullable types instead.

```kotlin
// CORRECT
require(width > 0) { "Width must be positive, got $width" }
val item = list.firstOrNull() ?: return emptyList()

// WRONG
val item = list.first!!
```

### No Broad Exception Catching

Never catch `Exception` or `Throwable` without re-throwing `CancellationException`.

```kotlin
// CORRECT
try {
    detector.detect(frame)
} catch (_: CancellationException) {
    throw CancellationException("Pipeline cancelled")
} catch (e: Exception) {
    when (e) {
        is IllegalStateException,
        is IllegalArgumentException -> throw e
        else -> null
    }
}

// WRONG
try {
    detector.detect(frame)
} catch (e: Exception) {
    return null  // swallows everything
}
```

### No Type Suppression

Never use `as any`, `@Suppress("UNCHECKED_CAST")` without justification, or `@ts-ignore` equivalents.

```kotlin
// WRONG
val items = data as List<Any>
```

---

## Naming Conventions

| Element | Convention | Example |
|:---|:---|:---|
| Class / Interface | PascalCase | `DetectorPlugin`, `SceneGraph` |
| Function | camelCase | `buildSceneGraph()`, `renderHaptic()` |
| Property | camelCase | `contentType`, `spatialThreshold` |
| Constant | SCREAMING_SNAKE_CASE | `EdgeGeneratorDefaults` (object), `API_LEVEL` |
| Enum value | SCREAMING_SNAKE_CASE | `LINE_CHART`, `FORMULA`, `SPATIAL` |
| Package | lowercase, no dots after `io.drishti` | `io.drishti.core`, `io.drishti.graph` |
| File | PascalCase, matches primary class | `Pipeline.kt`, `SceneGraph.kt` |

### Domain-Specific Terms

Use domain vocabulary, not generic names:

```kotlin
// CORRECT — domain vocabulary
val dataPoints: List<DataPoint>
val edges: List<SceneEdge>
val confidence: Float

// WRONG — generic names
val items: List<Any>
val connections: List<Map<String, Any>>
val score: Double
```

---

## SOLID Principles

### Single Responsibility

Every class has exactly one reason to change.

```kotlin
// CORRECT — Pipeline orchestrates detection + graph building
public class Pipeline(private val config: PipelineConfig = PipelineConfig()) {
    public suspend fun detect(...): List<ContentItem> { ... }
    public fun buildSceneGraph(...): SceneGraph { ... }
}

// WRONG — Pipeline does detection + rendering + persistence
public class Pipeline {
    fun detect(...) { ... }
    fun renderHaptics(...) { ... }
    fun saveToDatabase(...) { ... }
}
```

### Open/Closed

Extend through plugins, not modification.

```kotlin
// CORRECT — add new content type via plugin
class CircuitPlugin : DetectorPlugin {
    override val contentType = ContentType.CUSTOM("circuit")
    override suspend fun detect(frame: Frame): ContentItem? { ... }
}

// WRONG — add new type by modifying Pipeline
class Pipeline {
    fun detectGraph(...) { ... }
    fun detectFormula(...) { ... }
    fun detectCircuit(...) { ... }  // modifying core for new type
}
```

### Liskov Substitution

All implementations of an interface must be substitutable.

```kotlin
// Both GraphPlugin and FormulaPlugin implement DetectorPlugin
// and can be used interchangeably in Pipeline.detect()
val detectors: List<DetectorPlugin> = listOf(GraphPlugin(), FormulaPlugin())
pipeline.detect(frame, detectors)  // works with any DetectorPlugin
```

### Interface Segregation

Interfaces are small and focused.

```kotlin
// CORRECT — focused interfaces
public interface DetectorPlugin {
    val contentType: ContentType
    val confidence: Float
    suspend fun detect(frame: Frame): ContentItem?
}

public interface HapticsRenderer : RendererPlugin {
    fun renderHaptic(items: List<ContentItem>, focusIndex: Int): HapticOutput
}

// WRONG — fat interface
public interface Plugin {
    fun detect(frame: Frame): ContentItem?
    fun renderHaptics(items: List<ContentItem>): HapticOutput
    fun renderAudio(items: List<ContentItem>): AudioOutput
    fun renderVoice(items: List<ContentItem>): VoiceOutput
    fun saveState()
    fun loadState()
}
```

### Dependency Inversion

High-level modules depend on abstractions, not concretions.

```kotlin
// CORRECT — Pipeline depends on DetectorPlugin interface
public class Pipeline(private val config: PipelineConfig) {
    public suspend fun detect(frame: Frame, detectors: List<DetectorPlugin>): List<ContentItem>
}

// WRONG — Pipeline depends on concrete implementation
public class Pipeline {
    fun detect(frame: Frame): List<ContentItem> {
        val graphDetector = GraphDetector()  // concrete dependency
        return graphDetector.analyze(frame)
    }
}
```

---

## Documentation Standards

### KDoc

Every public API MUST have KDoc with `@param`, `@return`, and `@throws`.

```kotlin
/**
 * Run all detectors on the frame concurrently.
 *
 * Each [DetectorPlugin] produces at most one [ContentItem] per frame.
 * Null results (no detection) are filtered out, items below the
 * configured [PipelineConfig.minConfidence] threshold are dropped.
 *
 * @param frame The input image frame.
 * @param detectors List of detector plugins to run.
 * @return Non-null detected content items passing the confidence threshold.
 */
public suspend fun detect(frame: Frame, detectors: List<DetectorPlugin>): List<ContentItem>
```

### Comment Rules

- Never write comments that restate code: `// increment counter` before `counter++`
- Explain WHY, not WHAT
- Never leave `TODO`, `FIXME`, `HACK` without owner and date
- Never leave commented-out code

---

## Error Handling Patterns

### Sealed Classes for Domain Errors

```kotlin
// Use sealed classes, not strings
sealed class DetectionError {
    object NoDetectorsRegistered : DetectionError()
    data class InsufficientConfidence(val actual: Float, val required: Float) : DetectionError()
    data class UnsupportedFormat(val format: FrameFormat) : DetectionError()
}
```

### Result Pattern

Public API methods that can fail return `Result<T>`:

```kotlin
public fun haptics(): Result<HapticOutput> {
    val renderer = renderers.filterIsInstance<HapticsRenderer>().firstOrNull()
        ?: return Result.failure(IllegalStateException("No haptic renderer registered"))
    return try {
        Result.success(renderer.renderHaptic(contentItems))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Test Standards

### Naming Convention

```
GIVEN [setup] WHEN [action] THEN [expected result]
```

Or shorter:

```
WHEN [action] THEN [expected result]
```

### Required Test Coverage

Every module MUST test:
- Happy path
- Error path
- Edge cases (empty input, boundary values, null)
- Cancellation paths (for suspend functions)
- Thread safety (if concurrent)

### Example

```kotlin
class PipelineTest {
    @Test
    fun WHEN_empty_frame THEN_returns_empty_list() = runTest {
        val pipeline = Pipeline()
        val frame = Frame(0, 0, FrameFormat.RGB_888, null)
        val result = pipeline.detect(frame, listOf(mockDetector))
        assertTrue(result.isEmpty())
    }

    @Test
    fun WHEN_no_detectors THEN_returns_empty_list() = runTest {
        val pipeline = Pipeline()
        val frame = Frame(100, 100, FrameFormat.RGB_888, ByteArray(100))
        val result = pipeline.detect(frame, emptyList())
        assertTrue(result.isEmpty())
    }
}
```

---

## Build Quality Gates

Every PR MUST pass:

```bash
./gradlew assembleDebug testDebugUnitTest apiCheck
```

| Check | What it validates |
|:---|:---|
| `assembleDebug` | Code compiles without errors |
| `testDebugUnitTest` | All unit tests pass |
| `apiCheck` | No breaking changes to public API surface |

### Binary Compatibility

Public API is tracked via Binary Compatibility Validator (BCV). API dump files in `api/` directories MUST match compiled output. Breaking changes require a major version bump.

---

## Anti-AI-Slop Detection

AI-generated code has specific hallmarks. Check for:

| Pattern | Problem |
|:---|:---|
| Comments that restate code | `// increment counter` before `counter++` |
| Generic names | `result`, `data`, `handler`, `manager`, `output` |
| `try { ... } catch (e: Exception) { return null }` | Swallows all errors |
| `!!` bang operators | Crashes at runtime |
| Over-abstraction | `AbstractBaseFactoryProvider` patterns |
| Only happy-path tests | No edge cases, error paths |
| Sealed classes with "impossible" states | States that don't map to business rules |

---

## Checklist

Before submitting code:

- [ ] Explicit visibility modifier on every public declaration
- [ ] Explicit return type on every public function
- [ ] No `!!` bang operators
- [ ] No broad `catch (e: Exception)` without `CancellationException` re-throw
- [ ] KDoc on every public API with `@param`, `@return`
- [ ] No comments restating code
- [ ] No TODO/FIXME/HACK without owner+date
- [ ] All tests pass (`testDebugUnitTest`)
- [ ] API compatibility check passes (`apiCheck`)
- [ ] No unused imports
- [ ] No demo data or hardcoded values
