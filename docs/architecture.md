# Architecture Guide

This document describes the architecture of the Drishti SDK.

## Overview

The Drishti SDK is a plugin-based system for converting visual STEM content into multimodal sensory outputs. It consists of three main layers:

1. **Vision Layer** - Preprocesses visual input (images, camera frames)
2. **Detection Layer** - Identifies content types using plugins
3. **Rendering Layer** - Converts detected content into haptic, audio, or voice output

## Core Components

### Drishti Entry Point

The `Drishti` class is the main entry point. Use the Builder pattern to configure:

```kotlin
val drishti = Drishti.Builder()
    .addDetector(GraphPlugin())
    .addRenderer(HapticsPlugin())
    .build()
```

### Plugin Architecture

Plugins implement both detection and rendering interfaces:

- `DetectorPlugin` - Analyzes frames and returns `ContentItem` objects
- `HapticsRenderer` - Converts content to haptic pulses
- `AudioRenderer` - Converts content to spatial audio
- `VoiceOutputRenderer` - Converts content to speech

### Pipeline Flow

```
Input Frame
    ↓
Vision Preprocessing (ImagePreprocessor)
    ↓
Parallel Detection (all registered plugins)
    ↓
Content Items (GraphContent, FormulaContent, MoleculeContent)
    ↓
Scene Graph Construction
    ↓
Parallel Rendering (haptics, audio, voice)
    ↓
MultimodalOutput
```

## Module Structure

- `drishti-core` - Core interfaces and pipeline
- `drishti-vision` - Image preprocessing
- `drishti-graph` - Graph detection plugin
- `drishti-formula` - Formula detection plugin
- `drishti-molecule` - Molecule detection plugin
- `drishti-haptics` - Haptic rendering
- `drishti-audio` - Spatial audio rendering
- `drishti-voice` - Voice output
- `drishti-android` - Android platform integration
- `drishti-demo` - Demo application

## Data Flow

1. **Frame Input** - Raw image data from camera or file
2. **Preprocessing** - Normalization, feature extraction
3. **Detection** - Each plugin analyzes the frame
4. **Content Items** - Structured representation of detected content
5. **Rendering** - Convert to output modality
6. **Output** - Haptic pulses, audio samples, or speech text
