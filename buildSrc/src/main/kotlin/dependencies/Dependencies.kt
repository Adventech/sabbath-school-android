package dependencies

object Dependencies {
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"
    const val TIMBER = "com.jakewharton.timber:timber:${Versions.TIMBER}"
    const val PLAY_AUTH = "com.google.android.gms:play-services-auth:${Versions.PLAY_AUTH}"
    const val ANDROID_JOB = "com.evernote:android-job:${Versions.ANDROID_JOB}"
    const val JODA = "net.danlew:android.joda:${Versions.JODA}"
    const val COIL = "io.coil-kt:coil:${Versions.COIL}"
    const val TAP_TARGET = "uk.co.samuelwall:material-tap-target-prompt:${Versions.TAP_TARGET}"

    object Hilt {
        const val ANDROID = "com.google.dagger:hilt-android:${Versions.HILT}"
        const val COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    }

    object AndroidX {
        const val CORE = "androidx.core:core-ktx:${Versions.AndroidX.CORE}"
        const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.AndroidX.APPCOMPAT}"
        const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.AndroidX.RECYCLER_VIEW}"
        const val LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${Versions.AndroidX.LIFECYCLE_EXT}"
        const val LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_LIVEDATA = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_KTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.LIFECYCLE_KTX}"
        const val ACTIVITY = "androidx.activity:activity-ktx:${Versions.AndroidX.ACTIVITY}"
        const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.AndroidX.FRAGMENT}"
        const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.CONSTRAINT_LAYOUT}"
        const val START_UP = "androidx.startup:startup-runtime:${Versions.AndroidX.START_UP}"
        const val PREFERENCE = "androidx.preference:preference-ktx:${Versions.AndroidX.PREFERENCE}"
        const val BROWSER = "androidx.browser:browser:${Versions.AndroidX.BROWSER}"
        const val DATASTORE_PREFS = "androidx.datastore:datastore-preferences:${Versions.AndroidX.DATASTORE_PREFS}"
    }

    object Firebase {
        const val BOM = "com.google.firebase:firebase-bom:${Versions.FIREBASE_BOM}"
        const val ANALYTICS = "com.google.firebase:firebase-analytics-ktx"
        const val AUTH = "com.google.firebase:firebase-auth-ktx"
        const val DATABASE = "com.google.firebase:firebase-database-ktx"
        const val STORAGE = "com.google.firebase:firebase-storage-ktx"
        const val MESSAGING = "com.google.firebase:firebase-messaging-ktx"
        const val CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
    }

    object Kotlin {
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
        const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"
        const val COROUTINES_PLAY_SERVICES = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.COROUTINES}"
    }

    object Iconics {
        const val CORE = "com.mikepenz:iconics-core:${Versions.ICONICS}"
        const val VIEWS = "com.mikepenz:iconics-views:${Versions.ICONICS}"
        const val TYPEFACE = "com.mikepenz:iconics-typeface-api:${Versions.ICONICS}"
        const val MATERIAL_TYPEFACE = "com.mikepenz:google-material-typeface:4.0.0.1-kotlin@aar"
    }

    object Facebook {
        const val SDK = "com.facebook.android:facebook-android-sdk:${Versions.Facebook.SDK}"
        const val SHIMMER = "com.facebook.shimmer:shimmer:${Versions.Facebook.SHIMMER}"
    }

    object MarkWorm {
        const val core = "io.noties.markwon:core:${Versions.MARK_WORM}"
    }
}
