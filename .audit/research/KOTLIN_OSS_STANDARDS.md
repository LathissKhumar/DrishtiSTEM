# Kotlin KMP OSS Quality Standards Reference
Generated: 2026-06-29T20:55:00+05:30
Source: bg_34e0fb53 / ses_0ec0ba1f3ffeDq5mQMaYnzcN7G
Sources analyzed: Decompose (7K+ stars), moko-mvvm, KaMPKit

---

## MANDATORY Build Quality Gates

| Feature | What Production Libraries Do |
|---------|------------------------------|
| **Explicit API mode** | `explicitApi()` in kotlin block — forces `public`/`internal` on every declaration |
| **Binary compatibility** | `kotlinx.binary-compatibility-validator` plugin — tracks API surface |
| **Linting** | ktlint or detekt in `check` task |
| **License header** | Every `.kt` file starts with `/* Copyright... */` |
| **`allWarningsAsErrors`** | `compilerOptions.allWarningsAsErrors = true` |
| **`-Xjsr305=strict`** | Catches Java nullability mismatches at compile time |
| **Version catalog** | `libs.versions.toml` for dependency management |

---

## Public API Patterns (from Decompose, moko-mvvm)

### Interface-First Design
```kotlin
// Decompose StackNavigator — interface defines contract, extension functions provide convenience
interface StackNavigator<C : Any> {
    /**
     * Transforms the current stack of configurations to a new one.
     * @param transformer transforms the current configuration stack to a new one.
     * @param onComplete called when the navigation is finished.
     */
    fun navigate(
        transformer: (stack: List<C>) -> List<C>,
        onComplete: (newStack: List<C>, oldStack: List<C>) -> Unit,
    )
}
```

**Rules**:
- Interface defines contract, extension functions provide convenience
- Every parameter documented with `@param` KDoc
- Return type always explicit — never inferred
- Generic bounds always specified (`C : Any`)
- Function names are verbs: `navigate()`, `push()`, `pop()`, `subscribe()`

### Abstract Classes for Inheritance Control
```kotlin
// Decompose Value.kt — abstract class, not interface
// KDoc explains WHY: "since [Value] is a class (not an interface) with a generic type parameter,
// it is useful to expose state streams to ObjC/Swift."
abstract class Value<out T : Any> {
    abstract val value: T
    abstract fun subscribe(observer: (T) -> Unit): Cancellation
}
```

### Production Sealed State Pattern
```kotlin
// moko-mvvm ResourceState — TWO type parameters
sealed class ResourceState<out T, out E> {
    data class Success<out T, out E>(val data: T) : ResourceState<T, E>()
    data class Failed<out T, out E>(val error: E) : ResourceState<T, E>()
    class Loading<out T, out E> : ResourceState<T, E>()
    class Empty<out T, out E> : ResourceState<T, E>()

    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun dataValue(): T? = (this as? Success)?.data
    fun errorValue(): E? = (this as? Failed)?.error
}
```

### Opt-in Annotations (Decompose has 4 levels)
```kotlin
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
annotation class InternalDrishtiSdkApi  // never use externally

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class ExperimentalDrishtiSdkApi  // may change

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class FaultyDrishtiSdkApi  // known bugs

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class DelicateDrishtiSdkApi  // correct but easy to misuse
```

---

## Test Patterns (from Decompose)

### Naming: GIVEN/WHEN/THEN
```kotlin
@Suppress("TestFunctionName")
class MutableValueTest {
    private val value = MutableValue("0")

    @Test
    fun WHEN_created_THEN_initial_value() {
        assertEquals("0", value.value)
    }

    @Test
    fun GIVEN_unsubscribed_WHEN_value_changed_THEN_not_emitted() {
        val values = ArrayList<String>()
        val cancellation = value.subscribe { values += it }
        cancellation.cancel()
        values.clear()
        value.value = "1"
        assertContentEquals(emptyList(), values)
    }
}
```

**What distinguishes from AI-generated tests**:
1. `GIVEN_..._WHEN_..._THEN_...` naming
2. Tests cover unsubscribe/cancel paths — not just happy path
3. Tests verify side effects — emissions list, not just return values
4. `@Suppress("TestFunctionName")` — explicit opt-in to non-standard naming

---

## Anti-Patterns: AI-Generated vs Human-Written

| Anti-Pattern | AI-Generated Hallmark | Human OSS Pattern |
|---|---|---|
| **Comments** | `// Increment counter` restating code | `// Prevent race condition during concurrent subscription` explaining WHY |
| **Naming** | `result`, `data`, `handler`, `manager` | `breedViewState`, `stackNavigator`, `cancellation` |
| **Error handling** | `try { ... } catch (e: Exception) { return null }` | sealed class with specific error types, never swallow |
| **Null safety** | `!!` bang operator, `result ?: return null` | Nullable types with `?.let {}`, explicit `require()` |
| **Abstraction** | `AbstractBaseStrategyFactoryProvider` | `interface DogApi` → `class DogApiImpl : DogApi` |
| **Tests** | Only happy path, testing mocks | Edge cases, unsubscribe paths, error propagation |

---

## Kotlin API Design Rules (Official Guidelines)

- **Functions**: verbs — `navigate()`, `push()`, `pop()`, `subscribe()`
- **Properties**: nouns — `value`, `stack`, `isLoading`
- **Boolean getters**: `is` prefix — `isLoading()`, `isSuccess()`
- **Top-level functions** when state is passed as params
- **Extension functions** to add behavior to existing types
- **Lambda parameters last** for DSL-style APIs
- **`out`** covariance on return types, `in` contravariance on parameters
- **Never use `kotlin.Result` for domain errors** — custom sealed hierarchy instead
- **Provide both throwing and Result-returning variants** — suffix with `Catching`
- **`@Throws` for iOS interop** — so Swift sees `throws`

---

## DrishtiSDK Audit Checklist

- [ ] License header on every `.kt` file (`/* Copyright... */`)
- [ ] `explicitApi()` enabled in build.gradle.kts
- [ ] Explicit visibility on every public declaration (`public` keyword)
- [ ] Explicit return types on every public function/property
- [ ] KDoc on every public API with `@param`, `@return`, `@throws`
- [ ] `@RequiresOptIn` annotations for internal/experimental APIs
- [ ] No `!!` — use `require()`, `check()`, or nullable types
- [ ] No broad `try/catch` — catch specific exceptions or use sealed Result types
- [ ] Sealed classes for state — every subclass represents a real business state
- [ ] `data object` for singletons, `data class` for parameterized states
- [ ] Test naming: `GIVEN_..._WHEN_..._THEN_...` or `WHEN_..._THEN_...`
- [ ] Tests cover: happy path, error path, edge cases, cancellation
- [ ] Binary compatibility validator plugin active
- [ ] `allWarningsAsErrors` in compiler options
- [ ] No comments restating code — only explain WHY
- [ ] Interface-first design — contracts separate from implementations
- [ ] expect/actual used correctly — minimal public surface, platform APIs wrapped
