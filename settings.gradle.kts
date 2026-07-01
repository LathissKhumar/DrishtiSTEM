pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "drishti-stem"

// Module names match DrishtiSDK for JitPack compatibility.
// These directories are created as empty shells; actual source code lives in DrishtiSDK.
include(":drishti-core")
include(":drishti-vision")
include(":drishti-graph")
include(":drishti-formula")
include(":drishti-molecule")
include(":drishti-haptics")
include(":drishti-audio")
include(":drishti-voice")
include(":drishti-android")
include(":drishti-demo")
