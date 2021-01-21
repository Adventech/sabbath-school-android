/*
 * Copyright (c) 2020. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import dependencies.Dependencies
import extensions.addTestsDependencies
import java.io.FileInputStream
import java.util.Properties

plugins {
    id(BuildPlugins.ANDROID_APPLICATION)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
    id(BuildPlugins.KOTLIN_ALLOPEN)
    id(BuildPlugins.DAGGER_HILT)
    id(BuildPlugins.NAVIGATION_SAFE_ARGS)
    id(BuildPlugins.FIREBASE_CRASHLYTICS)
    id(BuildPlugins.GOOGLE_SERVICES)
}

allOpen {
    // allows mocking for classes w/o directly opening them for release builds
    annotation("com.cryart.sabbathschool.core.annotations.OpenClass")
}

fun readVersionCode(): Int {
    val file = file("build_number.properties")
    return if (file.exists()) {
        val keyProps = Properties().apply {
            load(FileInputStream(file))
        }
        val buildNumber = keyProps.getProperty("BUILD_NUMBER", "1")
        1490 + buildNumber.toInt()
    } else {
        1
    }
}

val useReleaseKeystore = file(BuildAndroidConfig.KEYSTORE_PROPS_FILE).exists()
val appVersionCode = readVersionCode()

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        applicationId = BuildAndroidConfig.APP_ID
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(BuildAndroidConfig.TARGET_SDK_VERSION)

        versionCode = appVersionCode
        versionName = "${BuildAndroidConfig.Version.name} ($appVersionCode)"

        testInstrumentationRunner = BuildAndroidConfig.TEST_INSTRUMENTATION_RUNNER

        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (useReleaseKeystore) {
            val keyProps = Properties().apply {
                load(FileInputStream(file(BuildAndroidConfig.KEYSTORE_PROPS_FILE)))
            }

            create(BuildType.RELEASE) {
                storeFile = file(keyProps.getProperty("release.keystore"))
                storePassword = keyProps.getProperty("release.keystore.password")
                keyAlias = keyProps.getProperty("key.alias")
                keyPassword = keyProps.getProperty("key.password")
            }
        }
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
            if (useReleaseKeystore) {
                signingConfig = signingConfigs.getByName(BuildType.RELEASE)
            }

            manifestPlaceholders["enableReporting"] = true
        }
        getByName(BuildType.DEBUG) {

            manifestPlaceholders["enableReporting"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.COROUTINES
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    packagingOptions {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        excludes += "/META-INF/AL2.0"
        excludes += "/META-INF/LGPL2.1"
    }
}

dependencies {

    implementation(project(BuildModules.Common.CORE))
    implementation(project(BuildModules.Common.DESIGN))
    implementation(project(BuildModules.Common.TRANSLATIONS))
    implementation(project(BuildModules.Features.LESSONS))
    implementation(project(BuildModules.Features.BIBLE))

    implementation(Dependencies.Kotlin.KOTLIN)

    implementation(Dependencies.MATERIAL)
    implementation(Dependencies.AndroidX.CORE)
    implementation(Dependencies.AndroidX.APPCOMPAT)
    implementation(Dependencies.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Dependencies.AndroidX.ACTIVITY)
    implementation(Dependencies.AndroidX.FRAGMENT_KTX)
    implementation(Dependencies.AndroidX.NAVIGATION_UI)
    implementation(Dependencies.AndroidX.NAVIGATION_FRAGMENT)
    implementation(Dependencies.AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(Dependencies.AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(Dependencies.AndroidX.LIFECYCLE_LIVEDATA)
    implementation(Dependencies.AndroidX.RECYCLER_VIEW)
    implementation(Dependencies.AndroidX.START_UP)

    implementation(Dependencies.Hilt.ANDROID)
    kapt(Dependencies.Hilt.COMPILER)

    implementation(Dependencies.Kotlin.COROUTINES)
    implementation(Dependencies.Kotlin.COROUTINES_ANDROID)
    implementation(Dependencies.Kotlin.COROUTINES_PLAY_SERVICES)

    implementation(Dependencies.PLAY_AUTH)

    implementation(platform(Dependencies.Firebase.BOM))
    implementation(Dependencies.Firebase.CORE)
    implementation(Dependencies.Firebase.ANALYTICS)
    implementation(Dependencies.Firebase.AUTH)
    implementation(Dependencies.Firebase.CRASHLYTICS)

    implementation(Dependencies.TIMBER)

    implementation(Dependencies.FACEBOOK)
    implementation(Dependencies.ANDROID_JOB)
    implementation(Dependencies.JODA)

    addTestsDependencies()
}
