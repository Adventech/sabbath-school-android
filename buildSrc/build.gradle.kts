plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    jcenter()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

object PluginVersions {
    const val GRADLE_ANDROID = "4.1.0"
    const val KOTLIN = "1.4.10"
    const val KTLINT = "0.39.0"
    const val HILT = "2.29.1-alpha"
    const val NAVIGATION = "2.3.1"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:${PluginVersions.NAVIGATION}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
}