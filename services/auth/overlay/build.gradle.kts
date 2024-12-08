plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.foundry.base)
}

foundry {
    features { compose() }
    android { features { robolectric() } }
}

dependencies {
    implementation(projects.common.designCompose)
    implementation(projects.common.translations)
    implementation(projects.libraries.circuit.api)

    implementation(libs.kotlinx.collectionsImmutable)
    implementation(libs.timber)
}
