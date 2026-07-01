# Image Preprocessing & OpenCV Integration Research
Generated: 2026-06-29T20:55:00+05:30
Source: bg_745c6178 / ses_0ec0bb55effenNzhMdwBV7VD06

## Key Finding: No production KMP library does OpenCV-level image processing in commonMain
Use expect/actual: pure Kotlin fallback in commonMain, OpenCV in androidMain.

---

## 1. OpenCV Android SDK Integration (androidMain)

Since OpenCV 4.9.0, available via Maven Central:
```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.opencv)  // org.opencv:opencv:4.13.0 — already in project!
}
```

**Initialization**:
```kotlin
import org.opencv.android.OpenCVLoader
if (!OpenCVLoader.initLocal()) {
    throw IllegalStateException("OpenCV failed to initialize")
}
```

---

## 2. YUV_420_888 to RGB Conversion (Critical Bridge)

CameraX outputs YUV_420_888, OpenCV works with RGB BGR Mat.

### PATTERN A: Set CameraX to RGBA_8888 (RECOMMENDED — simplest)
```kotlin
val imageAnalysis = ImageAnalysis.Builder()
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
    .setTargetResolution(Size(1280, 720))
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()

// Then direct Mat creation from RGBA bytes:
fun imageProxyToMatRGBA(imageProxy: ImageProxy): Mat {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val mat = Mat(imageProxy.height, imageProxy.width, CvType.CV_8UC4)
    mat.put(0, 0, bytes)
    return mat
}
```

### PATTERN B: Direct YUV → RGB Mat conversion
```kotlin
fun imageProxyToRgbMat(imageProxy: ImageProxy): Mat {
    val yuvMat = Mat(
        imageProxy.height + imageProxy.height / 2,
        imageProxy.width,
        CvType.CV_8UC1,
        imageProxyToByteBuffer(imageProxy)
    )
    val rgbMat = Mat(imageProxy.height, imageProxy.width, CvType.CV_8UC3)
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420)
    if (imageProxy.imageInfo.rotationDegrees != 0) {
        Core.rotate(rgbMat, rgbMat, imageProxy.imageInfo.rotationDegrees / 90 - 1)
    }
    return rgbMat
}
```

### PATTERN C: Handle interleaved UV (NV12/NV21)
```kotlin
fun Image.yuvToRgba(): Mat {
    val rgbaMat = Mat()
    val chromaPixelStride = planes[1].pixelStride
    if (chromaPixelStride == 2) {
        // Interleaved (NV12/NV21)
        val yMat = Mat(height, width, CvType.CV_8UC1, planes[0].buffer)
        val uvMat = Mat(height / 2, width / 2, CvType.CV_8UC2, planes[1].buffer)
        val addrDiff = planes[2].buffer.let { Mat(height / 2, width / 2, CvType.CV_8UC2, it) }
            .dataAddr() - uvMat.dataAddr()
        if (addrDiff > 0) {
            Imgproc.cvtColorTwoPlane(yMat, uvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
        } else {
            Imgproc.cvtColorTwoPlane(yMat, uvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
        }
    } else {
        // Planar (I420)
        val yuvBytes = ByteArray(width * (height + height / 2))
        // ... assemble Y, U, V planes
        val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuvMat.put(0, 0, yuvBytes)
        Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
    }
    return rgbaMat
}
```

---

## 3. OCR Preprocessing Pipeline with OpenCV

### Complete OCR-Ready Pipeline
```kotlin
fun preprocessForOCR(inputMat: Mat): Mat {
    // Step 1: Convert to grayscale
    val gray = Mat()
    if (inputMat.channels() > 1) {
        Imgproc.cvtColor(inputMat, gray, Imgproc.COLOR_BGR2GRAY)
    } else {
        inputMat.copyTo(gray)
    }

    // Step 2: Adaptive thresholding (handles varying lighting)
    val binary = Mat()
    Imgproc.adaptiveThreshold(
        gray, binary, 255.0,
        Imgproc.ADAPTIVE_THRESH_MEAN_C,
        Imgproc.THRESH_BINARY,
        15,   // blockSize (odd number, 15-30 for OCR)
        -2.0  // C constant (subtracted from mean)
    )

    // Step 3: Morphological open (removes small noise)
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0))
    val cleaned = Mat()
    Imgproc.morphologyEx(binary, cleaned, Imgproc.MORPH_OPEN, kernel)

    // Step 4: Dilate (connect broken text characters)
    val dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(1.0, 1.0))
    val dilated = Mat()
    Imgproc.dilate(cleaned, dilated, dilateKernel)

    return dilated
}
```

### Canny Edge Detection for Shapes
```kotlin
fun detectEdgesForShapes(inputMat: Mat): Mat {
    val gray = Mat()
    if (inputMat.channels() > 1) {
        Imgproc.cvtColor(inputMat, gray, Imgproc.COLOR_BGR2GRAY)
    } else {
        inputMat.copyTo(gray)
    }
    val blurred = Mat()
    Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
    val edges = Mat()
    Imgproc.Canny(blurred, edges, 0.0, 0.0, 3, true)  // useOtsu = true
    return edges
}
```

### Connected Components for Shape Detection
```kotlin
fun findConnectedComponents(binaryMat: Mat): List<Rect> {
    val labelImage = Mat(binaryMat.size(), CvType.CV_32S)
    val nLabels = Imgproc.connectedComponents(binaryMat, labelImage, 8)
    val boundingBoxes = mutableListOf<Rect>()
    for (label in 1 until nLabels) {  // skip background (label 0)
        val mask = Mat()
        Core.compare(labelImage, Scalar(label.toDouble()), mask, Core.CMP_EQ)
        val rect = Imgproc.boundingRect(mask)
        if (rect.width > 10 && rect.height > 10) boundingBoxes.add(rect)
        mask.release()
    }
    labelImage.release()
    return boundingBoxes
}
```

---

## 4. Recommended Architecture for DrishtiSDK

```kotlin
// commonMain: ImagePreprocessor.kt — pure Kotlin fallback
class ImagePreprocessor {
    fun grayscale(frame: Frame): ProcessedFrame {
        val data = frame.data ?: return ProcessedFrame(frame.width, frame.height, null, ProcessedFormat.GRAYSCALE)
        if (frame.format != FrameFormat.RGB_888) {
            return ProcessedFrame(frame.width, frame.height, data, ProcessedFormat.GRAYSCALE)
        }
        val gray = ByteArray(frame.width * frame.height)
        for (i in 0 until frame.width * frame.height) {
            val offset = i * 3
            if (offset + 2 >= data.size) break
            val r = data[offset].toInt() and 0xFF
            val g = data[offset + 1].toInt() and 0xFF
            val b = data[offset + 2].toInt() and 0xFF
            gray[i] = (0.299f * r + 0.587f * g + 0.114f * b).toInt().toByte()
        }
        return ProcessedFrame(frame.width, frame.height, gray, ProcessedFormat.GRAYSCALE)
    }
    // ... other methods with pure Kotlin implementations
}

// androidMain: AndroidImagePreprocessor.kt — OpenCV-powered
actual class AndroidImagePreprocessor actual constructor() {
    init {
        if (!OpenCVLoader.initLocal()) {
            throw IllegalStateException("OpenCV failed to initialize")
        }
    }
    // ... real OpenCV implementations
}
```

---

## 5. Key KMP Image Processing Libraries Evaluated

| Library | CommonMain? | OpenCV-level? | Notes |
|---------|-------------|---------------|-------|
| mihonapp/bitmap.kt | ✅ | ❌ Basic ops only | KMP wrapping Android Bitmap |
| KotlinMania/image-kotlin | ✅ | ⚠️ Decode/encode | Port of Rust image-rs |
| JetBrains/skiko | ✅ | ⚠️ Canvas/drawing | Skia bindings, not CV |
| OpenCV (JNI) | ❌ androidMain only | ✅ Full CV | Only practical option for real preprocessing |

**Bottom line**: Pure Kotlin alternatives don't match OpenCV performance. Use expect/actual.
