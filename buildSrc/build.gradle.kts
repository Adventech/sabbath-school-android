plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

object PluginVersions {
    const val GRADLE_ANDROID = "7.2.2"
    const val GRADLE_VERSIONS = "0.39.0"
    const val KOTLIN = "1.7.10"
    const val KTLINT = "0.40.0"
    const val HILT = "2.43.2"
    const val PAPARAZZI = "1.0.0"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
    implementation("com.pinterest:ktlint:${PluginVersions.KTLINT}")
    implementation("com.github.ben-manes:gradle-versions-plugin:${PluginVersions.GRADLE_VERSIONS}")
    implementation("app.cash.paparazzi:paparazzi-gradle-plugin:${PluginVersions.PAPARAZZI}")
}
