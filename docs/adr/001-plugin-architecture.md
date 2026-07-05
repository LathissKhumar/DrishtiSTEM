# ADR-001: Plugin-Based Architecture

## Status

Accepted

## Date

2026-06-29

## Context

The Drishti SDK needs to support multiple content types (graphs, formulas, molecules, circuits, maps, etc.) without modifying core code for each new type. The SDK must be extensible by third-party developers and maintain a stable public API.

## Decision

We adopt a plugin-based architecture where:

1. **Core knows nothing about specific content types.** `drishti-core` defines interfaces (`DetectorPlugin`, `RendererPlugin`), not implementations.
2. **Each content type is a separate module.** `drishti-graph`, `drishti-formula`, `drishti-molecule` are independent plugins.
3. **Plugins register via Builder pattern.** Applications configure the SDK by adding plugins:
   ```kotlin
   val drishti = Drishti.Builder()
       .addDetector(GraphPlugin())
       .addRenderer(HapticsPlugin())
       .build()
   ```
4. **Pipeline executes detectors concurrently.** All registered `DetectorPlugin` instances run in parallel via `coroutineScope` + `async`.
5. **SceneGraph unifies output.** Regardless of plugin type, all content items are represented as `SceneNode` objects with typed edges.

## Consequences

### Positive
- Adding a new content type requires zero changes to core code
- Plugins can be developed, tested, and published independently
- Applications only include plugins they need (modular distribution)
- Third-party developers can create custom plugins
- Public API remains stable across content type additions

### Negative
- Plugin discovery is explicit (no classpath scanning) — must register each plugin
- Cross-plugin communication requires going through the Pipeline (no direct calls)
- Plugin ordering may affect scene graph edge generation

### Mitigations
- Explicit registration is documented and validated by `PluginRegistry.validate()`
- Scene graph edges encode relationships between different content types
- Edge deduplication prevents duplicate connections

## Alternatives Considered

### 1. Module-per-type with switch statements
```kotlin
when (contentType) {
    GRAPH -> GraphDetector().detect(frame)
    FORMULA -> FormulaDetector().detect(frame)
    // ...
}
```
**Rejected:** Violates Open/Closed principle. Adding a new type requires modifying Pipeline.

### 2. Annotation-based auto-discovery
```kotlin
@ContentType("graph")
class GraphPlugin : DetectorPlugin { ... }
```
**Rejected:** Requires classpath scanning, adds complexity, breaks explicit registration model.

### 3. Monolithic module
All detectors and renderers in a single module.
**Rejected:** Violates single responsibility. Build times increase. Cannot publish independently.

## References

- [Architecture Guide](architecture.md)
- [Plugin Specification](plugin-spec.md)
- [drishti-core/Registry.kt](../../DrishtiSDK/drishti-core/src/commonMain/kotlin/io/drishti/core/Registry.kt)
