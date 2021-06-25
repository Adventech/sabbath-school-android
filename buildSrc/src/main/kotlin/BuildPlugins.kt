object BuildPlugins {
    object Android {
        const val APPLICATION = "com.android.application"
        const val LIBRARY = "com.android.library"
    }

    object Kotlin {
        const val ANDROID = "kotlin-android"
        const val PARCELIZE = "kotlin-parcelize"
        const val KAPT = "kotlin-kapt"
    }

    object Google {
        const val CRASHLYTICS = "com.google.firebase.crashlytics"
        const val SERVICES = "com.google.gms.google-services"
    }

    const val DAGGER_HILT = "dagger.hilt.android.plugin"
    const val KTLINT = "plugins.ktlint"

    const val UPDATE_DEPENDENCIES = "plugins.update-dependencies"
}
