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
    const val GRADLE_ANDROID = "7.3.1"
    const val KOTLIN = "1.7.20"
    const val KTLINT = "11.0.0"
    const val HILT = "2.44.1"
    const val PAPARAZZI = "1.1.0"
    const val BENCHMARK = "1.2.0-alpha07"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:${PluginVersions.KTLINT}")
    implementation("app.cash.paparazzi:paparazzi-gradle-plugin:${PluginVersions.PAPARAZZI}")
    implementation("androidx.benchmark:benchmark-gradle-plugin:${PluginVersions.BENCHMARK}")
}
