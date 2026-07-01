# Oboe & Spatial Audio Research for DrishtiSDK
Generated: 2026-06-30
Source: Web research, Context7 docs, GitHub production patterns

## Executive Summary

Oboe is Google's recommended C++ audio I/O library for Android low-latency apps. It wraps AAudio (API 27+) and OpenSL ES (older) with automatic fallback. For DrishtiSDK's accessibility-focused spatial audio, **Oboe is the correct choice** — but several production pitfalls must be addressed. The Android Spatializer API (API 33+) handles system-level HRTF/spatialization and should be used alongside Oboe for headphone spatial audio, not as a replacement.

---

## 1. AudioStreamBuilder Configuration — Production Patterns

### Canonical Production Builder Pattern

From Google's official docs and production code (Skia, JUCE, FluidSynth, OpenAL Soft):

```cpp
oboe::AudioStreamBuilder builder;
builder.setDirection(oboe::Direction::Output)
    ->setPerformanceMode(oboe::PerformanceMode::LowLatency)  // CRITICAL
    ->setSharingMode(oboe::SharingMode::Exclusive)           // CRITICAL
    ->setFormat(oboe::AudioFormat::Float)                    // Float for spatial processing
    ->setChannelCount(oboe::ChannelCount::Stereo)            // Stereo for spatial
    ->setDataCallback(this)                                  // Callback mode
    ->setErrorCallback(this)                                 // Error handling
    ->setUsage(oboe::Usage::AssistanceSonification)          // Accessibility use case
    ->setContentType(oboe::ContentType::Sonification);       // For accessibility apps
```

**Key points from [official docs](https://developer.android.com/games/sdk/oboe/low-latency-audio):**
- Use `PerformanceMode::LowLatency` + `SharingMode::Exclusive` for minimum latency
- Use `Usage::Game` or `Usage::AssistanceSonification` — NOT `Usage::Media`
- Float format recommended for spatial audio processing
- Let Oboe choose sample rate (defaults to 48000 Hz on most modern devices)

### Production examples from GitHub

**Google Skia** ([source](https://github.com/google/skia/blob/main/modules/audioplayer/SkAudioPlayer_oboe.cpp)):
```cpp
builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
builder.setSharingMode(oboe::SharingMode::Exclusive);
builder.setSampleRate(fReader->getSampleRate());
builder.setChannelCount(fReader->getNumChannels());
builder.setCallback(this);
builder.setFormat(oboe::AudioFormat::Float);
```

**OpenAL Soft** ([source](https://github.com/kcat/openal-soft/blob/master/alc/backends/oboe.cpp)):
```cpp
builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
builder.setUsage(oboe::Usage::Game);
builder.setSampleRateConversionQuality(oboe::SampleRateConversionQuality::None);
builder.setChannelConversionAllowed(false);
```

**FluidSynth** ([source](https://github.com/FluidSynth/fluidsynth/blob/master/src/drivers/fluid_oboe.cpp)):
```cpp
builder.setPerformanceMode(
    dev->performance_mode == 1 ? PerformanceMode::PowerSaving :
    dev->performance_mode == 2 ? PerformanceMode::LowLatency : PerformanceMode::None)
    ->setUsage(Usage::Media)
    ->setContentType(ContentType::Music)
    ->setCallback(dev->oboe_callback.get())
    ->setErrorCallback(dev->oboe_error_callback.get())
    ->setSampleRateConversionQuality(dev->srate_conversion_quality);
```

---

## 2. Callback Mode vs Blocking Mode

### Callback Mode (RECOMMENDED for low latency)

From [official Oboe docs](https://github.com/google/oboe/blob/main/docs/FullGuide.md):
> "To achieve the lowest possible latency with Oboe, use `PerformanceMode::LowLatency` in conjunction with a high-priority data callback."

**Requirements for callback mode:**
1. Implement `AudioStreamCallback` interface
2. Register via `builder.setDataCallback(this)`
3. Callback runs on a high-priority real-time thread
4. **Must NOT block** — no locks, no allocation, no I/O
5. **Must return quickly** — typically < 5ms processing

```cpp
class MyCallback : public oboe::AudioStreamCallback {
public:
    DataCallbackResult onAudioReady(
        oboe::AudioStream *oboeStream,
        void *audioData,
        int32_t numFrames) override {
        
        // Write audio data to audioData buffer
        // Return Continue to keep playing, Stop to halt
        
        // For output streams: fill audioData with samples
        // For input streams: read from audioData
        
        return DataCallbackResult::Continue;
    }
};
```

**Critical rule from [Oboe FAQ](https://github.com/google/oboe/wiki/TechNote_BufferTerminology):**
> "The callback function should not perform a read or write on the stream that invoked it."

For full-duplex (input + output): Register callback on OUTPUT stream only. Use non-blocking `read()` on input stream with `timeoutNanos=0`.

### Blocking Mode (Higher latency, simpler)

```cpp
// No callback — use write() instead
builder.setCallback(nullptr);

// Then write manually
stream->write(audioData, numFrames, timeoutNanos);
```

**When to use blocking mode:**
- Non-real-time audio (file playback)
- When callback latency is acceptable
- Testing/prototyping

**For DrishtiSDK spatial audio: ALWAYS use callback mode** — blocking mode cannot achieve MMAP (memory-mapped) low-latency path.

---

## 3. AudioTrack vs Oboe — Decision Matrix

| Factor | Oboe | AudioTrack |
|--------|------|-----------|
| **Latency** | ~20ms round-trip (with MMAP) | ~200ms default, ~40ms with PERF_LOW_LATENCY |
| **API** | C++ (NDK) | Java/Kotlin (SDK) |
| **MMAP Support** | Yes (exclusive mode) | Yes (API 27+, PerformanceMode.LOW_LATENCY) |
| **Stereo Spatial** | Native | Via Spatializer |
| **Backward Compat** | API 16+ (via OpenSL ES) | API 16+ |
| **Mixing** | Manual in callback | System mixer |
| **Format** | Float, I16, I24, I32 | I16, I24, I32, Float |
| **Head Tracking** | Not built-in | Via Spatializer API |

### When to use Oboe:
- Low-latency real-time audio (synthesizers, effects, spatial audio)
- Need exclusive MMAP access
- Custom audio processing pipeline
- Accessibility apps requiring < 30ms latency

### When to use AudioTrack:
- Simple media playback
- Need Java/Kotlin API only
- Don't need lowest latency
- System mixer integration needed

### For DrishtiSDK:
**Use Oboe** for the core spatial audio engine (C++ NDK). For Kotlin API exposure, wrap Oboe behind JNI with `AudioTrack` as a fallback for non-critical paths.

---

## 4. Common Oboe Pitfalls

### Pitfall 1: Sample Rate Mismatch (LATENCY KILLER)

**The #1 latency mistake.** From [Android dev docs](https://developer.android.com/games/sdk/oboe/low-latency-audio):

> "Use the natural sample rate of the device. You can do this by not specifying a sample rate, and you almost certainly get 48000 Hz."

**What happens:** If you specify 44100 Hz on a 48000 Hz device, the Android audio framework performs resampling — adding ~140ms latency.

**Solution:**
```cpp
// WRONG — forces resampling
builder.setSampleRate(44100);

// RIGHT — let device choose
// Don't call setSampleRate(), or:
builder.setSampleRate(kUnspecified);

// ALTERNATIVE — let Oboe do the conversion
builder.setSampleRateConversionQuality(SampleRateConversionQuality::Medium);
```

**Latency impact from [official benchmarks](https://developer.android.com/games/sdk/oboe/low-latency-audio):**
| Config | Round-trip Latency |
|--------|--------------------|
| 48000 Hz (native) | 20ms |
| 44100 Hz (AAudio resampling) | 160ms |
| 44100 Hz (Oboe SRC) | 23ms |

### Pitfall 2: Buffer Underruns

**Cause:** Callback takes too long or audio data not available when buffer is empty.

**Detection:**
```cpp
int32_t xRunCount = stream->getXRunCount();
if (xRunCount > previousXRunCount) {
    // Underrun occurred — increase buffer size or optimize callback
}
```

**Prevention:**
1. **Use double buffering:** `bufferSize = framesPerBurst * 2`
2. **Keep callback under 50% of burst time**
3. **Use `StabilizedCallback`** — Oboe's built-in load leveling:
```cpp
#include <oboe/StabilizedCallback.h>
auto stabilizedCallback = std::make_unique<oboe::StabilizedCallback>(myCallback);
builder.setCallback(stabilizedCallback.get());
```
4. **Avoid GC pauses** in callback (no JNI, no allocation)
5. **Don't specify `setFramesPerCallback`** unless you need it for FFT — let Oboe optimize

From [Google's RhythmGame sample](https://github.com/google/oboe/blob/main/samples/RhythmGame/src/main/cpp/Game.cpp):
```cpp
void Game::onErrorAfterClose(AudioStream *audioStream, Result error) {
    if (error == Result::ErrorDisconnected) {
        mGameState = GameState::Loading;
        mAudioStream.reset();
        mMixer.removeAllTracks();
        mCurrentFrame = 0;
        mSongPositionMs = 0;
        mLastUpdateTime = 0;
        start();  // Reopen and restart
    }
}
```

### Pitfall 3: Stream Disconnection

**When:** Device change (headphone plug/unplug), Bluetooth connect/disconnect.

**From [Oboe Tech Note](https://github.com/google/oboe/wiki/TechNote_Disconnect):**
> "If an app uses an error callback with AAudio, Oboe's AudioStreamErrorCallback methods are invoked. The app can reopen and start a new stream within the onErrorAfterClose() method."

**Production pattern** (from Snapcast, OpenAL Soft, Flycast):
```cpp
class MyErrorCallback : public oboe::AudioStreamErrorCallback {
public:
    void onErrorBeforeClose(oboe::AudioStream* stream, oboe::Result error) override {
        // Stream is disconnected but still open
        // Can query getXRunCount() here
        // Do NOT modify stream state
    }
    
    void onErrorAfterClose(oboe::AudioStream* stream, oboe::Result error) override {
        // Stream is stopped and closed
        if (error == oboe::Result::ErrorDisconnected) {
            // Restart the stream
            openStream();
            startStream();
        }
    }
};
```

### Pitfall 4: Channel Count Mismatch

**Issue:** Most devices support mono and stereo for low-latency. Some don't.

**Solution:**
```cpp
// For spatial audio, stereo is the minimum
builder.setChannelCount(oboe::ChannelCount::Stereo);

// If you need more channels, check capability first
// Most devices won't support >2 channels in low-latency mode
```

**From Oboe FAQ:** "On most devices and Android API levels it is possible to obtain a LowLatency stream for both mono and stereo, however, there are a few exceptions."

### Pitfall 5: Exclusive vs Shared Mode

```cpp
// Exclusive: lowest latency, but more likely to fail/disconnect
builder.setSharingMode(oboe::SharingMode::Exclusive);

// Shared: higher latency, but more stable
builder.setSharingMode(oboe::SharingMode::Shared);
```

**For accessibility apps:** Try Exclusive first, handle fallback to Shared:
```cpp
builder.setSharingMode(oboe::SharingMode::Exclusive);
Result result = builder.openStream(stream);
if (result != Result::OK) {
    // Fallback to shared mode
    builder.setSharingMode(oboe::SharingMode::Shared);
    result = builder.openStream(stream);
}
```

---

## 5. HRTF & Spatial Audio Integration

### Option A: Android Spatializer API (API 33+)

From [official Android docs](https://developer.android.com/media/grow/spatial-audio):

The `Spatializer` class provides **system-level HRTF spatialization** for multichannel audio over headphones:

```kotlin
val spatializer = audioManager.spatializer

// Check if spatialization is available
val isAvailable = spatializer.isEnabled 
    && spatializer.immersiveAudioLevel != Spatializer.SPATIALIZER_IMMERSIVE_LEVEL_NONE

// Check if a specific audio track can be spatialized
val canSpatialize = spatializer.canBeSpatialized(audioAttributes, audioFormat)

// Check for head tracking
val hasHeadTracking = spatializer.isHeadTrackerAvailable
```

**Architecture (Android 13+):**
```
App → Multichannel Audio → System Spatializer (HRTF) → Stereo Output → Headphones
                                    ↑
                            Head Tracker Sensor
```

**For DrishtiSDK:** Use Spatializer for system-level spatial audio when outputting multichannel. The Spatializer takes mixed audio content and renders a stereo stream with HRTF applied.

### Option B: Google Resonance Audio (HRTF + Ambisonics)

From [Resonance Audio docs](https://resonance-audio.github.io/resonance-audio/develop/overview.html):

> "Resonance Audio internally projects all sound sources into a global higher-order Ambisonic soundfield. This allows head-related transfer functions (HRTFs) to be applied just once to the soundfield rather than to individual sound sources within it."

**Integration pattern for native Android:**
1. Include Resonance Audio native library (C++)
2. Create `ResonanceAudioApi` instance
3. Create sound sources with positions
4. Set listener position/orientation
5. Render spatialized audio through Oboe

```cpp
#include <resonance_audio/resonance_audio_api.h>

// Create Resonance Audio engine
auto resonanceAudio = CreateResonanceAudioApi();

// Configure scene
resonanceAudio->SetRoomProperties(roomDimensions, roomMaterials);

// Create a sound source
int32_t sourceId;
resonanceAudio->CreateSoundObject(&sourceId);

// Set position (x, y, z in meters)
resonanceAudio->SetSoundObjectPosition(sourceId, x, y, z);

// In Oboe callback, render spatialized audio
resonanceAudio->SetHeadPosition(listenerX, listenerY, listenerZ);
resonanceAudio->SetHeadOrientation(forwardX, forwardY, forwardZ, upX, upY, upZ);
resonanceAudio->RenderAmbisonics(ambisonicBuffer, numFrames);
```

### Option C: Custom HRTF (Most flexible, most work)

For DrishtiSDK's accessibility use case where you need fine-grained control:

1. **Load HRTF dataset** (MIT KEMAR, CIPIC, or custom)
2. **Implement convolution** (overlap-save or partitioned convolution)
3. **Apply per-source HRTF** based on relative position to listener
4. **Handle head tracking** via sensor framework

```cpp
// Simplified HRTF rendering in Oboe callback
DataCallbackResult onAudioReady(AudioStream *stream, void *data, int32_t numFrames) {
    float *output = static_cast<float*>(data);
    
    for (each sound source) {
        // Calculate azimuth and elevation relative to listener
        auto [azimuth, elevation] = calculateRelativePosition(source, listener);
        
        // Select appropriate HRTF filters (interpolation between nearest)
        auto [leftIR, rightIR] = hrtfDatabase->getFilters(azimuth, elevation);
        
        // Convolve source audio with HRTF
        convolve(sourceAudio, leftIR, outputLeft);
        convolve(sourceAudio, rightIR, outputRight);
    }
    
    return DataCallbackResult::Continue;
}
```

### Recommendation for DrishtiSDK

**Layered approach:**
1. **System Spatializer** (API 33+) for general spatial audio over headphones
2. **Oboe** for low-latency I/O and custom audio processing
3. **Custom HRTF** for fine-grained spatial positioning (accessibility markers)
4. **Fallback** to binaural panning for older devices

---

## 6. Latency Optimization Checklist for DrishtiSDK

From [Android's official checklist](https://developer.android.com/games/sdk/oboe/low-latency-audio):

```
✅ Use Oboe API (C++ NDK)
✅ Set PerformanceMode::LowLatency
✅ Set SharingMode::Exclusive (with fallback to Shared)
✅ Use 48000 Hz or let Oboe handle sample rate conversion
✅ Set Usage::AssistanceSonification (accessibility app)
✅ Use data callbacks (not blocking write())
✅ Avoid blocking operations in callback
✅ Double-buffer: buffer size = 2 × burst size
✅ Handle stream disconnections via onErrorAfterClose()
✅ Monitor XRun count for buffer underruns
```

### Expected Latency (OboeTuner measurements):
| Configuration | Latency |
|---------------|---------|
| All recommendations followed | ~20ms |
| Performance mode not low latency | ~205ms |
| SHARED instead of EXCLUSIVE | ~26ms |
| 44100 Hz with AAudio resampling | ~160ms |
| 44100 Hz with Oboe SRC | ~23ms |
| Buffer size set to maximum | ~53ms |

---

## 7. Key Documentation Links

| Topic | URL |
|-------|-----|
| Oboe Full Guide | https://github.com/google/oboe/blob/main/docs/FullGuide.md |
| Oboe Getting Started | https://github.com/google/oboe/blob/main/docs/GettingStarted.md |
| Oboe API Reference | https://google.github.io/oboe/reference/ |
| Oboe FAQ | https://github.com/google/oboe/wiki/FAQ |
| Low Latency Checklist | https://developer.android.com/games/sdk/oboe/low-latency-audio |
| Buffer Terminology | https://github.com/google/oboe/wiki/TechNote_BufferTerminology |
| Disconnected Streams | https://github.com/google/oboe/wiki/TechNote_Disconnect |
| FullDuplexStream | https://github.com/google/oboe/wiki/Using-FullDuplexStream-for-Synchronized-IO |
| Android Spatial Audio | https://developer.android.com/media/grow/spatial-audio |
| Android Spatializer API | https://developer.android.com/reference/android/media/Spatializer |
| AOSP Spatial Audio Impl | https://source.android.google.cn/docs/core/audio/implement-spatial-audio |
| Resonance Audio | https://resonance-audio.github.io/resonance-audio/ |
| Resonance Audio C++ | https://github.com/resonance-audio/resonance-audio |
| Oboe RhythmGame Sample | https://github.com/google/oboe/tree/main/samples/RhythmGame |
| Oboe LiveEffect Sample | https://github.com/google/oboe/tree/main/samples/LiveEffect |
| Android NDK Audio Guide | https://developer.android.com/ndk/guides/audio/ |

---

## Action Items for DrishtiSDK AudioHAL.kt

1. **Audit AudioStreamBuilder configuration** — ensure LowLatency + Exclusive are set
2. **Add error callback** — handle stream disconnections for headphone plug/unplug
3. **Remove hardcoded sample rate** — let Oboe/device choose optimal
4. **Add XRun monitoring** — log buffer underruns for production diagnostics
5. **Add StabilizedCallback** wrapper for consistent timing
6. **Implement Spatializer API query** — check device capability before enabling spatial audio
7. **Add Resonance Audio integration** — for HRTF-based spatial positioning
8. **Document latency targets** — < 25ms for accessibility audio feedback
