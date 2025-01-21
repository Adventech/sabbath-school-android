plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.ss.readings"

    buildFeatures {
        viewBinding = true
    }
}

foundry {
    features { compose() }
    android { features { resources("ss_reading_") } }
}

dependencies {
    implementation(projects.common.core)
    implementation(projects.common.design)
    implementation(projects.common.designCompose)
    implementation(projects.common.translations)
    implementation(projects.common.lessonsData)
    implementation(projects.features.bible)
    implementation(projects.features.media)
    implementation(projects.features.reader)
    implementation(projects.libraries.foundation.android)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.lessons.api)
    implementation(projects.libraries.prefs.api)
    implementation(projects.services.media.ui)

    implementation(libs.google.material)
    implementation(libs.androidx.core)
    implementation(libs.timber)
    implementation(libs.coil.core)

    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.testUtils)
    testImplementation(projects.libraries.foundation.coroutines.test)
    testImplementation(projects.libraries.lessons.test)
}
