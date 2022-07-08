package dependencies

object Dependencies {
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"
    const val TIMBER = "com.jakewharton.timber:timber:${Versions.TIMBER}"
    const val PLAY_AUTH = "com.google.android.gms:play-services-auth:${Versions.PLAY_AUTH}"
    const val JODA = "net.danlew:android.joda:${Versions.JODA}"
    const val TAP_TARGET = "uk.co.samuelwall:material-tap-target-prompt:${Versions.TAP_TARGET}"
    const val PDF_KIT = "com.pspdfkit:pspdfkit:${Versions.pdfKit}"
    const val Cascade = "me.saket.cascade:cascade:${Versions.Cascade}"

    object Hilt {
        const val ANDROID = "com.google.dagger:hilt-android:${Versions.HILT}"
        const val COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
    }

    object AndroidX {
        const val ANNOTATIONS = "androidx.annotation:annotation:${Versions.AndroidX.ANNOTATIONS}"
        const val CORE = "androidx.core:core-ktx:${Versions.AndroidX.CORE}"
        const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.AndroidX.APPCOMPAT}"
        const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.AndroidX.RECYCLER_VIEW}"
        const val LIFECYCLE_COMMON = "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_COMPOSE = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${Versions.AndroidX.LIFECYCLE_EXT}"
        const val LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_KTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val ACTIVITY = "androidx.activity:activity-ktx:${Versions.AndroidX.ACTIVITY}"
        const val ACTIVITY_COMPOSE = "androidx.activity:activity-compose:${Versions.AndroidX.ACTIVITY}"
        const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.AndroidX.FRAGMENT}"
        const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.CONSTRAINT_LAYOUT}"
        const val START_UP = "androidx.startup:startup-runtime:${Versions.AndroidX.START_UP}"
        const val PREFERENCE = "androidx.preference:preference-ktx:${Versions.AndroidX.PREFERENCE}"
        const val BROWSER = "androidx.browser:browser:${Versions.AndroidX.BROWSER}"
        const val DATASTORE_PREFS = "androidx.datastore:datastore-preferences:${Versions.AndroidX.DATASTORE_PREFS}"
        const val MEDIA = "androidx.media:media:${Versions.AndroidX.MEDIA}"

        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.AndroidX.ROOM}"
            const val compiler = "androidx.room:room-compiler:${Versions.AndroidX.ROOM}"
            const val ktx = "androidx.room:room-ktx:${Versions.AndroidX.ROOM}"
        }
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
        const val MATERIAL_TYPEFACE = "com.mikepenz:google-material-typeface:4.0.0.2-kotlin@aar"
    }

    object Facebook {
        const val SHIMMER = "com.facebook.shimmer:shimmer:${Versions.Facebook.SHIMMER}"
    }

    object MarkWorm {
        const val core = "io.noties.markwon:core:${Versions.MARK_WORM}"
    }

    object Compose {
        private const val constraintLayoutVersion = "1.0.1"
        private const val material3Version = "1.0.0-alpha13"
        private const val snapperVersion = "0.2.1"

        const val foundation = "androidx.compose.foundation:foundation:${Versions.COMPOSE}"
        const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.COMPOSE}"
        const val ui = "androidx.compose.ui:ui:${Versions.COMPOSE}"
        const val uiUtil = "androidx.compose.ui:ui-util:${Versions.COMPOSE}"
        const val material = "androidx.compose.material:material:${Versions.COMPOSE}"
        const val material3 = "androidx.compose.material3:material3:$material3Version"
        const val windowSizeClass = "androidx.compose.material3:material3-window-size-class:$material3Version"
        const val runtime = "androidx.compose.runtime:runtime:${Versions.COMPOSE}"
        const val tooling = "androidx.compose.ui:ui-tooling:${Versions.COMPOSE}"
        const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:${Versions.COMPOSE}"
        const val icons = "androidx.compose.material:material-icons-core:${Versions.COMPOSE}"
        const val iconsExtended = "androidx.compose.material:material-icons-extended:${Versions.COMPOSE}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:$constraintLayoutVersion"
        const val snapper = "dev.chrisbanes.snapper:snapper:$snapperVersion"

        // TODO : Remove these dependencies once we upgrade to Android Studio Dolphin b/228889042
        // These dependencies are currently necessary to render Compose previews
        object Preview {
            const val customView = "androidx.customview:customview:1.2.0-alpha01"
            const val customViewContainer = "androidx.customview:customview-poolingcontainer:1.0.0-alpha01"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.0-beta01"
            const val savedState = "androidx.savedstate:savedstate-ktx:1.1.0"
        }
    }

    object Accompanist {
        const val placeholder = "com.google.accompanist:accompanist-placeholder-material:${Versions.ACCOMPANIST}"
        const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:${Versions.ACCOMPANIST}"
    }

    object Coil {
        const val core = "io.coil-kt:coil:${Versions.COIL}"
        const val compose = "io.coil-kt:coil-compose:${Versions.COIL}"
    }

    object Square {
        object Moshi {
            const val kotlin = "com.squareup.moshi:moshi-kotlin:${Versions.Square.moshi}"
            const val codegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.Square.moshi}"
        }
        object Okhttp {
            const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.Square.okhttp3}"
            const val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.Square.okhttp3}"
        }
        object Retrofit {
            const val retrofit2 = "com.squareup.retrofit2:retrofit:${Versions.Square.retrofit}"
            const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.Square.retrofit}"
        }
    }

    object ExoPlayer {
        const val core = "com.google.android.exoplayer:exoplayer-core:${Versions.exoplayer}"
        const val okhttp = "com.google.android.exoplayer:extension-okhttp:${Versions.exoplayer}"
        const val mediaSession = "com.google.android.exoplayer:extension-mediasession:${Versions.exoplayer}"
        const val ui = "com.google.android.exoplayer:exoplayer-ui:${Versions.exoplayer}"
    }
}
