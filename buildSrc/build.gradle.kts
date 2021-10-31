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
    const val GRADLE_ANDROID = "7.0.3"
    const val GRADLE_VERSIONS = "0.33.0"
    const val KOTLIN = "1.5.31"
    const val KTLINT = "0.40.0"
    const val HILT = "2.40" // Also update [Versions.kt]
    const val GOOGLE_SERVICES = "4.3.5"
    const val FIREBASE_CRASHLYTICS_GRADLE = "2.5.2"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
    implementation("com.pinterest:ktlint:${PluginVersions.KTLINT}")
    implementation("com.github.ben-manes:gradle-versions-plugin:${PluginVersions.GRADLE_VERSIONS}")
    implementation("com.google.gms:google-services:${PluginVersions.GOOGLE_SERVICES}")
    implementation("com.google.firebase:firebase-crashlytics-gradle:${PluginVersions.FIREBASE_CRASHLYTICS_GRADLE}")
}
