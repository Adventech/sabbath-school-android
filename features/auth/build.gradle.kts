plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "app.ss.auth"
}

foundry {
    features { compose() }
    android { features { robolectric() } }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.auth)
    implementation(libs.google.hilt.android)
    implementation(libs.google.id)
    implementation(libs.timber)
    implementation(projects.common.auth)
    implementation(projects.common.designCompose)
    implementation(projects.common.models)
    implementation(projects.common.translations)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.foundation.coroutines)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)

    ksp(libs.circuit.codegen)
    ksp(libs.google.hilt.compiler)
}
