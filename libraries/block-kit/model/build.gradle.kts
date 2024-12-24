plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.square.moshi.kotlin)
}
