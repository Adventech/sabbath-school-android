plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.android.library)
}

dependencies {
    implementation(projects.common.models)
    implementation(projects.libraries.circuit.api)
}
