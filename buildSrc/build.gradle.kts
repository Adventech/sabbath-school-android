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
    const val GRADLE_ANDROID = "4.1.2"
    const val GRADLE_VERSIONS = "0.33.0"
    const val KOTLIN = "1.4.21"
    const val KTLINT = "0.40.0"
    const val HILT = "2.31-alpha"
    const val NAVIGATION = "2.3.1"
    const val GOOGLE_SERVICES = "4.3.4"
    const val FIREBASE_CRASHLYTICS_GRADLE = "2.4.1"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${PluginVersions.KOTLIN}")
    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:${PluginVersions.NAVIGATION}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.HILT}")
    implementation("com.pinterest:ktlint:${PluginVersions.KTLINT}")
    implementation("com.github.ben-manes:gradle-versions-plugin:${PluginVersions.GRADLE_VERSIONS}")
    implementation("com.google.gms:google-services:${PluginVersions.GOOGLE_SERVICES}")
    implementation("com.google.firebase:firebase-crashlytics-gradle:${PluginVersions.FIREBASE_CRASHLYTICS_GRADLE}")
}
