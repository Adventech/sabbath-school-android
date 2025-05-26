plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.foundry.base)
}

foundry { features { compose() } }

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.test.androidx.compose.ui.junit)
    api(libs.test.androidx.ext)
    api(libs.test.robolectric)
    api(libs.test.roborazzi.compose)
    api(libs.test.roborazzi.rules)

    implementation(libs.test.roborazzi.core)
}
