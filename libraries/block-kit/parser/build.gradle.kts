plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.square.moshi.adapters)
    implementation(libs.square.moshi.kotlin)
    implementation(libs.timber)
    implementation(projects.libraries.blockKit.model)

    testImplementation(libs.bundles.testing.common)
}
