# Drishti Graph Module — Comprehensive Production Audit

**Date:** 2026-06-28
**Scope:** All 6 source files + 1 test file in `drishti-graph/src/commonMain/kotlin/io/drishti/graph/`
**Source location:** `/home/lathiss/Projects/DrishtiSDK/drishti-graph/`
**Criterion:** Open-source software quality standard; no AI-generated code smell

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Module Architecture](#module-architecture)
3. [Per-File Audit](#per-file-audit)
   - [GraphDataParser.kt](#1-graphdataparserkt)
   - [VegaLiteSpec.kt](#2vegalitespeckt)
   - [GraphDetector.kt](#3-graphdetectorkt)
   - [GraphRenderer.kt](#4-graphrenderekt)
   - [DataExtractor.kt](#5-dataextractorkt)
   - [GraphPlugin.kt](#6-graphpluginkt)
   - [GraphPluginTest.kt](#7-graphplugintestkt)
4. [Cross-Cutting Issues](#cross-cutting-issues)
5. [Priority Matrix](#priority-matrix)

---

## Executive Summary

The drishti-graph module converts structured graph data (JSON, CSV, programmatic) into multi-modal accessibility output (haptic, audio, voice) and Vega-Lite visual specs. The architecture is clean: `GraphDataParser` → `GraphContent` → `GraphRenderer` → outputs, with `GraphPlugin` as the public facade.

**Overall assessment:** The module is functional and well-structured, but has ~12 concrete issues that would prevent production use. The most critical are: a CSV parser that silently drops non-numeric data, Vega-Lite spec violations that will fail in real renderers, and exploration interfaces that ignore the direction parameter entirely.

### Issue Severity Counts

| Severity | Count |
|----------|-------|
| **Critical** (data loss / spec violation) | 3 |
| **High** (silent failure / wrong behavior) | 4 |
| **Medium** (quality / robustness) | 3 |
| **Low** (naming / style) | 2 |

---

## Module Architecture

```
Input (JSON/CSV/Programmatic)
        │
        ▼
  GraphDataParser          ← Parses string → GraphContent
        │
        ▼
    GraphContent            ← Core data model (GraphType, Axes, DataPoints)
        │
        ├──► GraphRenderer  ← Multi-modal output (haptic/audio/voice/vega)
        │
        └──► GraphPlugin    ← Public facade: DetectorPlugin + Renderers
```

**Dependencies:**
- `drishti-core`: ContentItem, ContentType, Frame, Axes, DataPoint, Output types
- `kotlinx.serialization.json`: JSON parsing
- `kotlinx.coroutines.core`: Suspend function in DetectorPlugin

---

## Per-File Audit

---

### 1. GraphDataParser.kt

**Lines:** 388 | **Role:** JSON/CSV → GraphContent conversion

#### Critical Issues

**C1. CSV parser silently drops non-numeric y-values (line 298-307)**

```kotlin
// CURRENT (line 298-307):
val numericDataPoints = input.data.mapIndexedNotNull { index, dp ->
    val yFloat = dp.y.toFloatOrNull()
    if (yFloat != null) {
        // ...
    } else {
        null  // ← SILENTLY DROPPED
    }
}
```

If CSV has a string y-column (e.g., `"High"`, `"Low"`, `"Medium"`), every row is silently dropped. The parser returns an empty `GraphContent` with no warning. This is a data-loss bug.

**Proposed fix:**
```kotlin
val numericDataPoints = input.data.mapIndexedNotNull { index, dp ->
    val yFloat = dp.y.toFloatOrNull()
    if (yFloat != null) {
        val xFloat = dp.x.toFloatOrNull() ?: index.toFloat()
        val label = dp.label ?: dp.x
        DataPoint(x = xFloat, y = yFloat, label = label)
    } else {
        // Preserve as categorical: assign ordinal x, keep y as label
        null
    }
}

// ADD: After the mapping, check for dropped points and warn
val droppedCount = input.data.size - numericDataPoints.size
if (droppedCount > 0) {
    warnings.add("$droppedCount data points with non-numeric y-values were excluded")
}
```

**C2. `parseCsvLine` doesn't handle escaped quotes (line 362-379)**

```kotlin
// CURRENT:
char == '"' -> inQuotes = !inQuotes
```

RFC 4180 CSV requires `""` to represent a literal quote inside a quoted field. The current parser treats `""` as two separate quote toggles, which works coincidentally for simple cases but breaks for fields like `"She said ""hello"""`.

**Proposed fix:**
```kotlin
private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    val chars = line.iterator()

    while (chars.hasNext()) {
        val char = chars.next()
        when {
            char == '"' -> {
                if (inQuotes && chars.hasNext() && chars.next() == '"') {
                    current.append('"') // Escaped quote
                } else {
                    inQuotes = !inQuotes
                }
            }
            char == ',' && !inQuotes -> {
                result.add(current.toString())
                current.clear()
            }
            else -> current.append(char)
        }
    }
    result.add(current.toString())
    return result
}
```

#### High Issues

**H1. `resolveGraphType` silently falls back to LINE_CHART for unknown types (line 336-346)**

```kotlin
else -> GraphType.LINE_CHART
```

If a user passes `"stacked_bar_chart"` or a typo like `"lien_chart"`, it silently becomes a line chart. This should either throw or log a warning.

**Proposed fix:**
```kotlin
internal fun resolveGraphType(typeStr: String): GraphType {
    return when (typeStr.lowercase().trim()) {
        "line_chart", "line", "linechart" -> GraphType.LINE_CHART
        "bar_chart", "bar", "barchart" -> GraphType.BAR_CHART
        "pie_chart", "pie", "piechart" -> GraphType.PIE_CHART
        "scatter_plot", "scatter", "scatterplot" -> GraphType.SCATTER_PLOT
        "area_chart", "area", "areachart" -> GraphType.AREA_CHART
        "histogram", "hist" -> GraphType.HISTOGRAM
        else -> throw GraphDataException(
            "Unknown chart type: '$typeStr'. " +
            "Valid types: line_chart, bar_chart, pie_chart, scatter_plot, area_chart, histogram"
        )
    }
}
```

**H2. `inferChartType` uses arbitrary thresholds (line 348-360)**

```kotlin
dataPoints.size in 2..8 && allNumeric -> "bar_chart"
dataPoints.size > 20 -> "scatter_plot"
```

These heuristics are fragile. 9 numeric points becomes a line chart, but 8 becomes a bar chart. 20 points forces scatter regardless of actual shape. This should be documented as "best-effort" or removed.

**Proposed fix:** Add a doc comment acknowledging the heuristic nature and keep the logic but add a warning:
```kotlin
/**
 * Best-effort chart type inference from data shape.
 *
 * Heuristics:
 * - Labeled + non-numeric → pie_chart
 * - 2-8 numeric points → bar_chart
 * - >20 numeric points → scatter_plot
 * - Otherwise → line_chart
 *
 * These are rough heuristics. Callers should prefer explicit type specification.
 */
internal fun inferChartType(dataPoints: List<DataPointInput>): String {
    // ... existing logic
}
```

#### Medium Issues

**M1. `buildGraphContent` computes x-range from numeric x-values only (line 312-316)**

When x-values are strings (e.g., "Jan", "Feb"), `toFloatOrNull()` returns null and `index.toFloat()` is used. But the x-range is computed from these index-based values, not the original string positions. This means the range is `0f..N-1f` which is correct for indexing but the axis label suggests otherwise.

**Proposed fix:** This is acceptable behavior but should be documented:
```kotlin
// When x-values are non-numeric, they're mapped to ordinal indices (0, 1, 2, ...)
// and the x-range reflects these indices, not the original string values.
```

**M2. `GraphDataInput` uses `@Serializable` but is only used internally (line 28-36)**

The `@Serializable` annotation on `GraphDataInput` and `DataPointInput` is unnecessary — these classes are never serialized, only deserialized via manual `JsonObject` parsing. The annotation adds unused generated code.

**Proposed fix:** Remove `@Serializable` from `GraphDataInput` and `DataPointInput`:
```kotlin
// REMOVE @Serializable from both classes
data class GraphDataInput(
    val type: String = "line_chart",
    // ...
)
```

#### Low Issues

**L1. `GraphDataException` extends `IllegalArgumentException` (line 385-388)**

This is semantically wrong. `IllegalArgumentException` implies the caller passed bad arguments. A custom exception class extending `RuntimeException` (or a sealed hierarchy) would be more appropriate.

**Proposed fix:**
```kotlin
class GraphDataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
```

---

### 2. VegaLiteSpec.kt

**Lines:** 389 | **Role:** Vega-Lite v5 spec generation

#### Critical Issues

**C3. Histogram puts `bin: true` in the mark instead of encoding (line 112-115)**

```kotlin
// CURRENT:
GraphType.HISTOGRAM -> buildJsonObject {
    put("type", "bar")
    put("bin", true)  // ← WRONG LOCATION
}
```

In Vega-Lite v5, binning belongs in the **encoding**, not the mark. The correct spec is:

```json
{
  "mark": "bar",
  "encoding": {
    "x": {"field": "x", "bin": true, "type": "quantitative"},
    "y": {"aggregate": "count", "type": "quantitative"}
  }
}
```

**Proposed fix:**
```kotlin
// In buildMark:
GraphType.HISTOGRAM -> buildJsonObject {
    put("type", "bar")
}

// In buildEncoding, add histogram-specific handling:
private fun buildEncoding(graph: GraphContent): JsonObject {
    return when (graph.graphType) {
        GraphType.PIE_CHART -> buildPieEncoding(graph)
        GraphType.HISTOGRAM -> buildHistogramEncoding(graph)
        else -> buildCartesianEncoding(graph)
    }
}

private fun buildHistogramEncoding(graph: GraphContent): JsonObject {
    return buildJsonObject {
        put("x", buildJsonObject {
            put("field", "x")
            put("bin", true)
            put("type", "quantitative")
            put("title", graph.axes.x.label.ifEmpty { "X" })
        })
        put("y", buildJsonObject {
            put("aggregate", "count")
            put("type", "quantitative")
            put("title", "Count")
        })
    }
}
```

#### High Issues

**H3. `cornerRadiusTopLeft`/`cornerRadiusTopRight` may not work on all bar marks (line 92-96)**

Vega-Lite v5 supports `cornerRadiusTopLeft` and `cornerRadiusTopRight` on bar marks, but only when the bar has a zero baseline or the scale starts at 0. If the y-scale domain doesn't start at 0, these properties have no effect (the "top" is ambiguous). This is correct behavior but undocumented.

**Proposed fix:** Add a comment:
```kotlin
// Note: cornerRadius properties only apply when bars extend from zero baseline.
// If the y-scale domain doesn't include 0, bars render without rounded corners.
```

**H4. `isNumericAxis` heuristic is fragile (line 212-214)**

```kotlin
private fun isNumericAxis(range: ClosedFloatingPointRange<Float>): Boolean {
    return range.start != 0f || range.endInclusive != 100f
}
```

This returns `false` for the default range `0f..100f`, which means any axis with the default range is treated as nominal. But a user might have actual numeric data that happens to range from 0 to 100.

**Proposed fix:** This heuristic is fundamentally flawed. The axis type should be determined by the data, not the range:
```kotlin
private fun isNumericAxis(dataPoints: List<DataPoint>, axis: Axis): Boolean {
    // If any x-value is non-numeric, treat as nominal
    return dataPoints.all { it.label == null || it.x.toFloatOrNull() != null }
}
```

Or better: let the caller specify axis type explicitly in `GraphContent`.

#### Medium Issues

**M3. `computeTrend` uses first/last points only (line 286-306)**

The trend computation only considers the first and last y-values, ignoring all intermediate points. A dataset `[10, 100, 10]` would be classified as STABLE (first=10, last=10, changeRatio=0) despite having a large spike.

**Proposed fix:** Use linear regression or at minimum consider the full dataset:
```kotlin
private fun computeTrend(dataPoints: List<DataPoint>): TrendResult {
    if (dataPoints.size < 2) return TrendResult(TrendDirection.STABLE, 0f)

    // Simple linear regression slope
    val n = dataPoints.size.toFloat()
    val sumX = dataPoints.indices.sum().toFloat()
    val sumY = dataPoints.sumOf { it.y.toDouble() }.toFloat()
    val sumXY = dataPoints.mapIndexed { i, p -> i * p.y }.sum()
    val sumX2 = dataPoints.indices.sumOf { it * it }.toFloat()

    val denominator = n * sumX2 - sumX * sumX
    if (denominator == 0f) return TrendResult(TrendDirection.STABLE, 0f)

    val slope = (n * sumXY - sumX * sumY) / denominator
    val yRange = dataPoints.maxOf { it.y } - dataPoints.minOf { it.y }
    if (yRange == 0f) return TrendResult(TrendDirection.STABLE, 0f)

    val normalizedSlope = slope * dataPoints.size / yRange

    return when {
        normalizedSlope > 0.1f -> TrendResult(TrendDirection.INCREASING, normalizedSlope.coerceIn(0f, 1f))
        normalizedSlope < -0.1f -> TrendResult(TrendDirection.DECREASING, (-normalizedSlope).coerceIn(0f, 1f))
        else -> TrendResult(TrendDirection.STABLE, (1f - kotlin.math.abs(normalizedSlope)).coerceIn(0f, 1f))
    }
}
```

**M4. `formatNumber` uses `%02f` which is locale-dependent (line 383-389)**

```kotlin
"%.2f".format(value)
```

On some locales, `%.2f` produces `3,14` instead of `3.14`. For a library, this should use `DecimalFormat` with explicit locale, or just use `kotlin.math.round`:

**Proposed fix:**
```kotlin
private fun formatNumber(value: Float): String {
    val rounded = (value * 100).toLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
```

#### Low Issues

**L2. `generateAccessibilityDescription` and `generateVoiceDescription` in GraphRenderer duplicate logic**

Both functions in `VegaLiteSpec.kt` and `GraphRenderer.kt` produce nearly identical descriptions. The `generateAccessibilityDescription` function in `VegaLiteSpec.kt` (line 318-370) and `generateVoiceDescription` in `GraphRenderer.kt` (line 270-302) have duplicated chart type labels, axis descriptions, and trend reporting.

**Proposed fix:** Extract a shared description builder or have `GraphRenderer.generateVoiceDescription` delegate to `generateAccessibilityDescription`:
```kotlin
// In GraphRenderer.kt
private fun generateVoiceDescription(graph: GraphContent, summary: DataSummary): String {
    return generateAccessibilityDescription(graph, summary)
}
```

---

### 3. GraphDetector.kt

**Lines:** 200 | **Role:** DetectorPlugin implementation + data-first entry points

#### High Issues

**H5. `detect(frame)` always returns null — violates interface contract (line 45-47)**

```kotlin
override suspend fun detect(frame: Frame): ContentItem? {
    return null // Vision-based detection removed; use data-first methods
}
```

The `DetectorPlugin` interface declares `detect(frame)` as the primary detection method. Returning `null` always means the detector is functionally dead for any code that uses the `DetectorPlugin` interface polymorphically. This is a design debt issue.

**Proposed fix:** Either:
1. Make `GraphDetector` NOT implement `DetectorPlugin` (cleanest), or
2. Add OCR-based detection as a real implementation:
```kotlin
override suspend fun detect(frame: Frame): ContentItem? {
    if (!frame.isNotEmpty()) return null
    // Delegate to OCR pipeline if available
    // For now, return null with a clear TODO
    return null
}
```

**H6. `detectFromOcrText` regex is overly broad (line 188-198)**

```kotlin
val numberPattern = Regex("""-?\d+\.?\d*""")
```

This matches any two numbers on a line as a data pair. A line like `"Revenue was $1,234.56 in 2024"` would produce a pair `(1234.56, 2024)` which is nonsensical.

**Proposed fix:** Require numbers to be separated by whitespace or delimiter, not just co-occur:
```kotlin
val pairPattern = Regex("""(-?\d+\.?\d*)\s*[,\t]\s*(-?\d+\.?\d*)""")
val matches = pairPattern.findAll(line).toList()
if (matches.size >= 1) {
    val (x, y) = matches[0].destructured
    pairs.add((x.toFloatOrNull() ?: return@mapNotNull null) to 
              (y.toFloatOrNull() ?: return@mapNotNull null))
}
```

#### Medium Issues

**M5. `extractNumericPairs` returns `List<Pair<Number, Number>>` but type is ambiguous (line 183-198)**

```kotlin
it.value.toFloatOrNull() ?: it.value.toDoubleOrNull() ?: return@map null
```

The `?.toFloatOrNull() ?: .toDoubleOrNull()` chain means some pairs use Float and others use Double. This inconsistency can cause issues downstream.

**Proposed fix:** Standardize on Float since `DataPoint` uses Float:
```kotlin
val numbers = numberPattern.findAll(line).map {
    it.value.toFloatOrNull()
}.filterNotNull().toList()
```

---

### 4. GraphRenderer.kt

**Lines:** 339 | **Role:** Multi-modal output generation

#### High Issues

**H7. Exploration interfaces ignore `direction` parameter (line 204-232)**

```kotlin
override fun renderExplorationHaptic(item: ContentItem, direction: ExplorationDirection): HapticOutput {
    return when (item) {
        is GraphContent -> renderer.renderHaptic(item)  // ← direction ignored
        else -> HapticOutput(pulses = emptyList(), pattern = "exploration")
    }
}
```

All three exploration methods (`renderExplorationHaptic`, `renderExplorationAudio`, `renderExplorationVoice`) delegate to the non-exploration renderers, completely ignoring the `direction` parameter. An exploration with `NEXT` vs `PREVIOUS` vs `POSITION` should produce different output (e.g., highlighting the next data point, scrolling through points, jumping to a specific index).

**Proposed fix:** Implement directional exploration:
```kotlin
override fun renderExplorationHaptic(item: ContentItem, direction: ExplorationDirection): HapticOutput {
    return when (item) {
        is GraphContent -> renderExplorationHapticImpl(item, direction)
        else -> HapticOutput(pulses = emptyList(), pattern = "exploration")
    }
}

private fun renderExplorationHapticImpl(
    graph: GraphContent,
    direction: ExplorationDirection
): HapticOutput {
    // For exploration, generate a single pulse for the current focus point
    // or a sequence for NEXT/PREVIOUS navigation
    val pulses = when (direction) {
        ExplorationDirection.NEXT -> {
            // Generate pulses for the next subset of data points
            graph.dataPoints.take(3).map { point ->
                HapticPulse(
                    intensity = normalizeIntensity(point.y, graph.axes.y.range),
                    duration = 50L,
                    x = normalizePosition(point.x, graph.axes.x.range),
                    y = normalizePosition(point.y, graph.axes.y.range)
                )
            }
        }
        ExplorationDirection.PREVIOUS -> {
            graph.dataPoints.takeLast(3).map { point ->
                HapticPulse(
                    intensity = normalizeIntensity(point.y, graph.axes.y.range),
                    duration = 50L,
                    x = normalizePosition(point.x, graph.axes.x.range),
                    y = normalizePosition(point.y, graph.axes.y.range)
                )
            }
        }
        ExplorationDirection.POSITION -> {
            // Single pulse at center for position exploration
            listOf(HapticPulse(intensity = 0.5f, duration = 100L, x = 0.5f, y = 0.5f))
        }
    }
    return HapticOutput(pulses = pulses, pattern = "graph_exploration_${direction.name.lowercase()}")
}
```

#### Medium Issues

**M6. Audio frequency range 200-1000Hz is narrow (line 335-338)**

```kotlin
return 200f + (normalized * 800f) // 200Hz to 1000Hz
```

This maps all data to a 2-octave range. For graphs with many data points, adjacent values may be indistinguishable. Production sonification tools typically use 100-4000Hz or wider.

**Proposed fix:** Make the frequency range configurable:
```kotlin
companion object {
    /** Default minimum frequency for audio sonification (Hz). */
    const val DEFAULT_MIN_FREQUENCY = 200f

    /** Default maximum frequency for audio sonification (Hz). */
    const val DEFAULT_MAX_FREQUENCY = 1000f
}

private fun mapToFrequency(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    minFreq: Float = DEFAULT_MIN_FREQUENCY,
    maxFreq: Float = DEFAULT_MAX_FREQUENCY
): Float {
    val normalized = normalizeIntensity(value, range)
    return minFreq + (normalized * (maxFreq - minFreq))
}
```

**M7. `graphTypeLabel` and `formatNumber` are duplicated from VegaLiteSpec.kt (line 304-321)**

Both `GraphRenderer` and `VegaLiteSpec` define their own `graphTypeLabel()` and `formatNumber()` functions. These should be extracted to a shared utility or the functions in `VegaLiteSpec.kt` should be made `internal` and reused.

**Proposed fix:** Move to a shared file or make VegaLiteSpec functions internal:
```kotlin
// In VegaLiteSpec.kt, change visibility:
internal fun graphTypeLabel(graphType: GraphType): String { ... }
internal fun formatNumber(value: Float): String { ... }

// In GraphRenderer.kt, use:
import io.drishti.graph.graphTypeLabel
import io.drishti.graph.formatNumber
```

---

### 5. DataExtractor.kt

**Lines:** 220 | **Role:** Unified extraction facade

#### Low Issues

**L3. `fromProperties` `data` field parsing is fragile (line 129-146)**

```kotlin
"data" -> {
    try {
        val dataElement = Json.parseToJsonElement(value)
        put(key, dataElement)
    } catch (e: Exception) {
        // Treat as comma-separated values
        val points = value.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size >= 2) {
                buildJsonObject {
                    put("x", parts[0].trim())
                    put("y", parts[1].trim())
                }
            } else null
        }
        put(key, JsonArray(points))
    }
}
```

The fallback parsing uses `;` as pair separator and `,` as x/y separator. This is undocumented and conflicts with CSV conventions. Also, the catch block catches ALL exceptions, not just JSON parse errors.

**Proposed fix:** Narrow the catch and document the format:
```kotlin
"data" -> {
    try {
        val dataElement = Json.parseToJsonElement(value)
        put(key, dataElement)
    } catch (e: IllegalArgumentException) {
        // Fallback: semicolon-separated pairs, comma-separated x,y
        // Format: "x1,y1;x2,y2;x3,y3"
        val points = value.split(";").mapNotNull { pair ->
            val parts = pair.split(",", limit = 2)
            if (parts.size == 2) {
                buildJsonObject {
                    put("x", parts[0].trim())
                    put("y", parts[1].trim())
                }
            } else null
        }
        put(key, JsonArray(points))
    }
}
```

The `DataExtractor` class itself is well-structured — it's a thin facade that delegates to `GraphDataParser` and wraps results in `ExtractionResult`. No major issues beyond the above.

---

### 6. GraphPlugin.kt

**Lines:** 233 | **Role:** Public facade implementing all interfaces

#### Low Issues

**L4. `GraphPlugin` implements `DetectorPlugin` but `detect()` is dead code**

This is the same issue as H5 but at the facade level. `GraphPlugin` implements `DetectorPlugin`, `HapticsRenderer`, `AudioRenderer`, and `VoiceOutputRenderer`. The `DetectorPlugin.detect()` method is dead code. Consider whether `GraphPlugin` should implement `DetectorPlugin` at all, or if the data-first methods should be on a separate interface.

**Proposed fix:** If `DetectorPlugin` implementation is required for plugin registration, keep it but add a clear `@Deprecated` or KDoc:
```kotlin
/**
 * Detect content from a camera frame.
 *
 * @deprecated This method always returns null. Use [fromJson], [fromCsv],
 * or [fromDataPoints] for data-first graph detection.
 */
@Deprecated(
    message = "Use data-first methods (fromJson, fromCsv, fromDataPoints)",
    replaceWith = ReplaceWith("fromJson(json)")
)
override suspend fun detect(frame: Frame): ContentItem? {
    return detector.detect(frame)
}
```

The rest of `GraphPlugin` is clean — it properly delegates to `GraphDetector` and `GraphRenderer`, handles empty input cases, and combines multiple graph items in the renderer interfaces.

---

### 7. GraphPluginTest.kt

**Lines:** 990 | **Role:** Test suite

#### Issues

**M8. Tests use `assertNotNull` + field access instead of smart-cast patterns**

Many tests do:
```kotlin
val graph = plugin.fromJson(json)
assertNotNull(graph)
assertEquals(GraphType.LINE_CHART, graph.graphType)
```

After `assertNotNull`, `graph` is still declared as `GraphContent?` in Kotlin's type system. The tests work because Kotlin's `assertNotNull` from `kotlin.test` doesn't smart-cast. This is fine but could be cleaner:

**Proposed fix:** Use `require` or `check` for cleaner test code:
```kotlin
val graph = plugin.fromJson(json) ?: fail("fromJson returned null")
assertEquals(GraphType.LINE_CHART, graph.graphType)
```

**M9. No negative tests for CSV with non-numeric data**

The test suite doesn't test what happens when CSV contains string y-values (the C1 issue). Adding a test would expose the silent data loss:

**Proposed fix:** Add test:
```kotlin
@Test
fun parseCsvWithNonNumericYValuesReportsWarning() {
    val csv = """
    Category,Rating
    A,High
    B,Low
    """.trimIndent()

    val result = parser.parseCsv(csv)
    assertTrue(result.warnings.isNotEmpty())
    assertTrue(result.graph.dataPoints.isEmpty())
}
```

**M10. No tests for edge cases**

Missing tests for:
- Very large datasets (1000+ points)
- Negative numbers in data
- Scientific notation (`1e5`, `3.14e-2`)
- Unicode characters in labels
- Empty data arrays
- Single data point

---

## Cross-Cutting Issues

### 1. No Structured Logging

The module uses `warnings.add()` to accumulate parse warnings, but there's no logging framework integration. Production code should use `kotlin-logging` or similar to emit warnings to the platform's logging system.

**Recommendation:** Add a `Logger` interface:
```kotlin
fun interface Logger {
    fun warn(message: String)
    fun error(message: String, cause: Throwable? = null)
}

class GraphDataParser(private val logger: Logger = Logger { }) {
    // Use logger.warn() instead of warnings.add()
}
```

### 2. No Input Validation Beyond Emptiness

`parseJson` checks for empty input and valid JSON syntax, but doesn't validate:
- `data` array contains at least one point
- `y` values are numeric for numeric chart types
- Chart type is valid before parsing data

**Recommendation:** Add a validation pass after parsing:
```kotlin
fun validateGraphContent(graph: GraphContent): List<String> {
    val issues = mutableListOf<String>()
    if (graph.dataPoints.isEmpty()) {
        issues.add("Graph has no data points")
    }
    if (graph.axes.x.range.start > graph.axes.x.range.endInclusive) {
        issues.add("X-axis range is inverted")
    }
    // ...
    return issues
}
```

### 3. Thread Safety

`GraphDataParser` and `GraphRenderer` are stateless classes (no mutable fields), so they're thread-safe. `GraphPlugin` composes them without mutable state. This is good — no issues here.

### 4. API Surface

The public API is well-designed with clear entry points:
- `GraphPlugin` for end users
- `GraphDetector` for data-first workflows
- `GraphRenderer` for custom rendering
- `GraphDataParser` for direct parsing

However, `GraphDataParser` is not `internal` — it's public. Consider making it `internal` if it's only meant to be used through `DataExtractor` and `GraphDetector`.

---

## Priority Matrix

| # | Severity | File | Issue | Fix Effort |
|---|----------|------|-------|------------|
| C1 | Critical | GraphDataParser | Silent CSV data loss | Small |
| C2 | Critical | GraphDataParser | CSV escaped quotes broken | Small |
| C3 | Critical | VegaLiteSpec | Histogram bin in wrong location | Small |
| H5 | High | GraphDetector | detect() always null | Medium |
| H7 | High | GraphRenderer | Exploration ignores direction | Medium |
| H1 | High | GraphDataParser | Silent fallback for unknown types | Small |
| H3 | High | VegaLiteSpec | cornerRadius docs missing | Tiny |
| H4 | High | VegaLiteSpec | isNumericAxis heuristic fragile | Small |
| M3 | Medium | VegaLiteSpec | Trend uses first/last only | Small |
| M4 | Medium | VegaLiteSpec | formatNumber locale-dependent | Tiny |
| M6 | Medium | GraphRenderer | Audio frequency range narrow | Tiny |
| M7 | Medium | GraphRenderer | Duplicated helpers | Small |
| L1 | Low | GraphDataParser | Wrong exception base class | Tiny |
| L2 | Low | VegaLiteSpec | Duplicated description builders | Small |
| L3 | Low | DataExtractor | Fragile properties parsing | Small |
| L4 | Low | GraphPlugin | Dead detect() code | Tiny |

---

## Research-Backed Findings

The following section incorporates findings from research into production Vega-Lite v5 specifications and graph sonification accessibility patterns.

### Vega-Lite v5 Specification Validation

Research from official Vega-Lite documentation confirms the following about our generated specs:

**C3 Confirmed — Histogram `bin` location is wrong:**
The official Vega-Lite documentation states: *"bin applies default binning parameters"* and must be placed in the encoding channel, not the mark object. Our current code puts `bin: true` in the mark definition, which will cause renderers to ignore it or produce errors.

**Valid Histogram Spec (from Vega-Lite examples):**
```json
{
  "mark": "bar",
  "encoding": {
    "x": {"bin": true, "field": "value", "type": "quantitative"},
    "y": {"aggregate": "count"}
  }
}
```

**H3 Partially Confirmed — `cornerRadiusTopLeft`/`cornerRadiusTopRight` are valid:**
Vega-Lite does support these properties on bar marks. However, they only apply when bars extend from a zero baseline. If the y-scale domain doesn't include 0, the "top" corner is ambiguous and these properties have no effect. The code is correct but undocumented.

**Area Chart `interpolate` is valid:**
The research confirms `"interpolate": "monotone"` is a valid mark property for area charts. Valid methods include: `linear`, `monotone`, `basis`, `cardinal`, `step`, `step-before`, `step-after`.

**`width`/`height` at top level are valid:**
These work correctly for single-view specifications and control the data plotting area, not the total visualization size.

### Graph Sonification Accessibility Research

Research from Brown et al. (2003), Flowers (2005), Nees & Walker (2007), and the ChartA11y paper (Zhang et al. 2024) provides actionable patterns:

**M6 Confirmed — Audio frequency range is too narrow:**
Our current range of 200-1000Hz is suboptimal. Research recommends:
- **MIDI range 35–100** (~62 Hz to ~2,637 Hz)
- **Practical sweet spot: C3 (130.81 Hz) to C5 (523.25 Hz)** for mobile speakers
- Avoid below MIDI 35 (hardware can't reproduce reliably) or above MIDI 100 (unpleasant to listen to)

**Recommended timing parameters:**
- **50–70ms between successive tones** for overview playback
- **1–4 data points per second** optimal for point estimation tasks
- Total sonification duration: **3–10 seconds** to fit within non-categorical echoic memory

**Critical pitfalls identified (from Flowers 2005):**
1. **Too many simultaneous streams**: "Attending to three or more continuous streams of sonified data is extremely difficult"
2. **Over-optimism about data bandwidth**: "There continues to be an over-optimism about human 'data bandwidth'"
3. **Inappropriate generalization from visualization**: Audio is not a drop-in replacement for visual charts

**H7 Confirmed — Exploration direction is critical:**
Apple's VoiceOver Audio Graphs (AXChart) provide the gold standard:
- **Continuous tone** modulates up/down with y-values
- **User drags finger** to control playback speed (throttle technique)
- **VoiceOver reads data point values** at current position
- **Pause on hold, resume on release**

Android lacks a native equivalent, requiring custom implementation.

### Recommended Audio Configuration

Based on research, the `GraphRenderer` should adopt:

```kotlin
companion object {
    /** Default minimum frequency (C3 = 130.81 Hz). */
    const val DEFAULT_MIN_FREQUENCY = 130.81f

    /** Default maximum frequency (C5 = 523.25 Hz). */
    const val DEFAULT_MAX_FREQUENCY = 523.25f

    /** Default interval between data points in playback (ms). */
    const val DEFAULT_POINT_INTERVAL_MS = 60L

    /** Maximum sonification duration (ms) to fit within echoic memory. */
    const val MAX_SONIFICATION_DURATION_MS = 10_000L
}
```

### Recommended Exploration Implementation

Based on ChartA11y (2024) and Apple's AXChart:

```kotlin
enum class ExplorationMode {
    OVERVIEW,      // Fast playback (8 pts/sec), captures trend
    DETAILED,      // Slow playback (2 pts/sec), point-by-point with labels
    INTERACTIVE    // User-controlled scrubbing
}

data class ExplorationConfig(
    val mode: ExplorationMode = ExplorationMode.OVERVIEW,
    val hapticOnPointContact: Boolean = true,
    val voiceLabelsOnScrub: Boolean = true
)
```

---

*End of audit.*
