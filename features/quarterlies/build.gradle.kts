plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.ss.quarterlies"
}

foundry {
    features { compose() }
    android { features { robolectric() } }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(projects.common.auth)
    implementation(projects.common.core)
    implementation(projects.common.designCompose)
    implementation(projects.common.lessonsData)
    implementation(projects.common.translations)
    implementation(projects.libraries.appWidget.api)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.lessons.api)
    implementation(projects.libraries.prefs.api)
    implementation(projects.services.auth.overlay)

    implementation(libs.coil.core)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    ksp(libs.circuit.codegen)

    implementation(libs.kotlinx.collectionsImmutable)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.appWidget.testFixtures)
    testImplementation(projects.libraries.testUtils)
    testImplementation(projects.libraries.foundation.coroutines.test)
    testImplementation(projects.libraries.lessons.test)
}
