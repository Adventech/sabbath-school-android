plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
}

foundry {
    features { compose() }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(libs.google.hilt.android)
    implementation(libs.timber)

    implementation(projects.common.designCompose)
    implementation(projects.libraries.blockKit.model)
    implementation(projects.libraries.cascadeCompose)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.foundation.android)

    ksp(libs.circuit.codegen)
    ksp(libs.google.hilt.compiler)
}
