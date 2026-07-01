# OCR Engine Research for DrishtiSDK Vision Module
Generated: 2026-06-29T20:55:00+05:30
Source: bg_f99cb38d / ses_0ec0bde58ffezeBg7GPqHECs2P

## Recommendation: ML Kit Text Recognition v2 (bundled) → PaddleOCR PP-OCRv6 (upgrade path)

---

## Engine Comparison

### Google ML Kit Text Recognition v2
- **Version**: `com.google.mlkit:text-recognition:16.0.1`
- **Latency**: 30-50ms per frame on modern hardware (real-time capable)
- **Accuracy**: High for Latin script, good for CJK
- **Offline**: Yes (bundled = ~4MB/script; unbundled = ~260KB via Play Services)
- **minSdk**: API 23
- **Output hierarchy**: `TextBlock → Line → Element → Symbol` with bounding boxes, confidence, rotation
- **Scripts**: Latin, Chinese, Devanagari, Japanese, Korean
- **KMP**: MLKit-KMP library exists (RufenKhokhar/MLKit-KMP)

**Key API pattern**:
```kotlin
val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
val image = InputImage.fromBitmap(bitmap, rotationDegrees)
val result = recognizer.process(image).await()
for (block in result.textBlocks) {
    for (line in block.lines) {
        for (element in line.elements) {
            // element.text, element.boundingBox, element.confidence
        }
    }
}
```

**CameraX integration**:
```kotlin
val imageAnalyzer = ImageAnalysis.Builder()
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
    .setTargetResolution(Size(1280, 720))
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()

class TextAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { result -> onResult(result.text) }
            .addOnCompleteListener { imageProxy.close() } // ALWAYS close!
    }
}
```

### Tesseract4Android
- **Version**: `4.9.0` (Tesseract OCR 5.5.1, Leptonica 1.85.0)
- **Latency**: 200-800ms per image (NOT real-time for camera)
- **Accuracy**: Good for clean printed text, poor for complex layouts
- **Offline**: 100% offline
- **Bundle size**: ~15-25MB with language data
- **minSdk**: API 21
- **Thread safety**: TessBaseAPI is NOT thread-safe — separate instances needed

### PaddleOCR PP-OCRv6
- **Version**: PP-OCRv6 (released June 11, 2026) via PaddleOCR v3.7.0
- **Latency**: ~420ms total pipeline on OnePlus 7 (Detection 349ms + Recognition 66ms)
- **Accuracy**: BEST IN CLASS — 86.2% detection Hmean, 83.2% recognition (beats Qwen3-VL-235B, GPT-5.5)
- **Offline**: 100% via ONNX Runtime
- **Bundle size**: Tiny (1.5M params) / Small (7.7M params) / Medium (34.5M params)
- **Languages**: 50 in single model
- **Already in deps**: ONNX Runtime 1.21.1 is in libs.versions.toml

---

## STEM Content Accuracy Reality

**None of these engines natively handle math formulas, chemical structures, or diagrams well.**

| Content Type | ML Kit | Tesseract | PaddleOCR |
|---|---|---|---|
| Plain printed text | ★★★★★ | ★★★★ | ★★★★★ |
| Handwritten text | ★★ | ★ | ★★ |
| Math formulas (LaTeX) | ★★ | ★★ | ★★★ |
| Chemical structures | ★ | ★ | ★★ |
| Tables | ★★ | ★★ | ★★★★ |
| Diagrams/flowcharts | ★ | ★ | ★★ |

**For STEM, OCR engine is only step 1.** Need post-processing:
- Detect formula regions vs text regions
- Convert formula images to LaTeX (UniMERNet)
- Parse chemical structures to SMILES (MolScribe)
- Convert diagrams to structured descriptions

---

## ML Kit Integration Steps for DrishtiSDK

### Add to libs.versions.toml
```toml
[versions]
mlkit-text-recognition = "16.0.1"

[libraries]
mlkit-text-recognition = { module = "com.google.mlkit:text-recognition", version.ref = "mlkit-text-recognition" }
```

### Add to drishti-vision/build.gradle.kts
```kotlin
implementation(libs.mlkit.text.recognition)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2") // for .await()
```

### KMP expect/actual Architecture
```kotlin
// commonMain — interface
interface OcrEngine {
    suspend fun recognizeText(frame: Frame): OcrResult
    fun close()
}

// androidMain — ML Kit impl
class MlKitOcrEngine : OcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    override suspend fun recognizeText(frame: Frame): OcrResult = withContext(Dispatchers.Default) {
        // Convert Frame to InputImage, process, map results
    }
    override fun close() { recognizer.close() }
}
```

### OCR Preprocessing Pipeline (4-stage)
1. **Deskewing** — Hough Transform rotation correction (±45°)
2. **Adaptive Binarization** — Gaussian-weighted local thresholding (blockSize=15, C=-2)
3. **Noise Removal** — Non-Local Means Denoising (h=10) before binarization + morphological open
4. **Sharpening** — Unsharp mask for thin strokes (subscripts, superscripts)

Preprocessing improves Tesseract F1 from 0.163 to 0.729 (+347%).
