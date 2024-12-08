plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.foundry.base)
}

foundry {
    features { compose() }
}

dependencies {
    implementation(projects.common.designCompose)
    implementation(projects.common.translations)
    implementation(projects.libraries.circuit.api)
}
