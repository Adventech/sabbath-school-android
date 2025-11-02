plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

foundry { features { compose() } }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.kotlinx.collectionsImmutable)
    implementation(libs.square.moshi.adapters)
    implementation(libs.square.moshi.kotlin)
    implementation(libs.timber)
    implementation(projects.libraries.blockKit.model)

    testImplementation(libs.bundles.testing.common)
}
