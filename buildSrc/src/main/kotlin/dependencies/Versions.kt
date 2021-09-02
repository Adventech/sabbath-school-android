package dependencies

object Versions {
    const val COROUTINES = "1.5.1"
    const val COMPOSE = "1.0.1"
    const val MATERIAL = "1.4.0"
    const val ACCOMPANIST = "0.17.0"

    object AndroidX {
        const val APPCOMPAT = "1.3.1"
        const val RECYCLER_VIEW = "1.2.1"
        const val LIFECYCLE = "2.3.1"
        const val LIFECYCLE_KTX = "2.4.0-alpha01"
        const val LIFECYCLE_EXT = "2.2.0"
        const val CORE = "1.6.0"
        const val FRAGMENT = "1.3.6"
        const val ACTIVITY = "1.3.1"
        const val CONSTRAINT_LAYOUT = "2.1.0"
        const val START_UP = "1.1.0"
        const val PREFERENCE = "1.1.1"
        const val BROWSER = "1.3.0"
        const val DATASTORE_PREFS = "1.0.0"
        const val MEDIA = "1.4.1"
        const val ROOM = "2.3.0"
    }

    object Facebook {
        const val SDK = "11.2.0"
        const val SHIMMER = "0.5.0"
    }

    const val HILT = "2.38.1" // Also update [build.gradle.kts]

    const val TIMBER = "5.0.1"
    const val COIL = "1.3.2"
    const val TAP_TARGET = "3.3.0"
    const val ICONICS = "5.3.0@aar"

    const val FIREBASE_BOM = "28.3.1"
    const val PLAY_AUTH = "19.2.0"
    const val ANDROID_JOB = "1.4.2"
    const val JODA = "2.10.9.1"
    const val JODA_TIME = "2.10.10"
    const val MARK_WORM = "4.6.2"
    const val exoplayer = "2.15.0"

    object Square {
        const val moshi = "1.12.0"
        const val okhttp3 = "4.9.1"
        const val retrofit = "2.9.0"
    }

    // Tests
    const val ANDROIDX_TEST = "1.4.0"
    const val EXT = "1.1.2"
    const val ARCH_CORE = "2.1.0"
    const val JUNIT = "4.13.2"
    const val ROBOELECTRIC = "4.6.1"
    const val MOCKK = "1.12.0"
    const val ESPRESSO = "3.4.0"
    const val FRAGMENT_TEST = "1.3.6"
    const val KLUENT = "1.68"
    const val TURBINE = "0.6.0"
}
