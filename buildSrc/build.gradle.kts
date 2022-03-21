plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/andreyberyukhov/FlowReactiveNetwork")
}

object PluginVersions {
    const val GRADLE_ANDROID = "7.0.4"
    const val GRADLE_VERSIONS = "0.39.0"
    const val KOTLIN = "1.6.10"
    const val KTLINT = "0.40.0"
    const val HILT = "2.41" // Also update [Versions.kt]
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
    implementation("com.pinterest:ktlint:${PluginVersions.KTLINT}")
    implementation("com.github.ben-manes:gradle-versions-plugin:${PluginVersions.GRADLE_VERSIONS}")
}
