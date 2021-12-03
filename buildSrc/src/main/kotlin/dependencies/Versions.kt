package dependencies

object Versions {
    const val COROUTINES = "1.5.2"
    const val COMPOSE = "1.0.5"
    const val MATERIAL = "1.4.0"
    const val ACCOMPANIST = "0.20.2"

    object AndroidX {
        const val APPCOMPAT = "1.4.0"
        const val RECYCLER_VIEW = "1.2.1"
        const val LIFECYCLE = "2.4.0"
        const val LIFECYCLE_EXT = "2.2.0"
        const val CORE = "1.7.0"
        const val FRAGMENT = "1.4.0"
        const val ACTIVITY = "1.4.0"
        const val CONSTRAINT_LAYOUT = "2.1.2"
        const val START_UP = "1.1.0"
        const val PREFERENCE = "1.1.1"
        const val BROWSER = "1.4.0"
        const val DATASTORE_PREFS = "1.0.0"
        const val MEDIA = "1.4.3"
        const val ROOM = "2.3.0"
    }

    object Facebook {
        const val SDK = "11.2.0"
        const val SHIMMER = "0.5.0"
    }

    const val HILT = "2.40.4" // Also update [build.gradle.kts]

    const val TIMBER = "5.0.1"
    const val COIL = "1.4.0"
    const val TAP_TARGET = "3.3.2"
    const val ICONICS = "5.3.3@aar"

    const val FIREBASE_BOM = "29.0.1"
    const val PLAY_AUTH = "19.2.0"
    const val JODA = "2.10.12.2"
    const val JODA_TIME = "2.10.13"
    const val MARK_WORM = "4.6.2"
    const val exoplayer = "2.16.1"
    const val pdfKit = "8.0.1"

    object Square {
        const val moshi = "1.12.0"
        const val okhttp3 = "4.9.3"
        const val retrofit = "2.9.0"
    }

    // Tests
    const val ANDROIDX_TEST = "1.4.0"
    const val EXT = "1.1.3"
    const val ARCH_CORE = "2.1.0"
    const val JUNIT = "4.13.2"
    const val ROBOELECTRIC = "4.7.3"
    const val MOCKK = "1.12.1"
    const val ESPRESSO = "3.4.0"
    const val FRAGMENT_TEST = "1.4.0"
    const val KLUENT = "1.68"
    const val TURBINE = "0.7.0"
}
