# Building from Source

This guide explains how to build the Drishti SDK from source.

## Prerequisites

- JDK 21
- Android SDK with API 35
- Android Studio Ladybug or later
- Git

## Clone the Repository

```bash
git clone https://github.com/lathiss/DrishtiSTEM.git
cd DrishtiSTEM
```

## Environment Setup

### Linux/macOS

```bash
export ANDROID_HOME=$HOME/android-sdk
export JAVA_HOME=/path/to/jdk-21
```

### Windows

```cmd
set ANDROID_HOME=%USERPROFILE%\android-sdk
set JAVA_HOME=C:\path\to\jdk-21
```

## Build

### Command Line

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :drishti-core:build

# Clean build
./gradlew clean build
```

### Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the DrishtiSDK directory
4. Wait for Gradle sync to complete
5. Build the project using Build > Make Project

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :drishti-core:test

# Run specific test class
./gradlew :drishti-core:test --tests "io.drishti.core.DrishtiTest"
```

## Publishing

### Local Publishing

```bash
./gradlew publishToMavenLocal
```

### JitPack

1. Tag a release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. JitPack will automatically build and publish

### Maven Central

1. Configure signing in `gradle.properties`
2. Run:
   ```bash
   ./gradlew publishToSonatype
   ```

## IDE Setup

### Android Studio

1. Install Android Studio Ladybug or later
2. Install Kotlin Multiplatform plugin
3. Open the project
4. Enable "Configure on demand" in Settings > Build > Compiler

### IntelliJ IDEA

1. Install IntelliJ IDEA Ultimate
2. Install Kotlin Multiplatform plugin
3. Open the project
4. Import Gradle project

## Troubleshooting

### Build Fails with JDK Version Error

Ensure you're using JDK 21:

```bash
java -version
# Should show: openjdk version "21.x.x"
```

### Gradle Sync Fails

1. Invalidate caches: File > Invalidate Caches / Restart
2. Re-import project: File > New > Import Project
3. Check Android SDK location in Project Structure

### Tests Fail

1. Ensure Android SDK is installed
2. Check device/emulator is running
3. Verify permissions in AndroidManifest.xml
