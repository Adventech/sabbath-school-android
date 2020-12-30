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

plugins {
    id(BuildPlugins.ANDROID_APPLICATION)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
    id(BuildPlugins.DAGGER_HILT)
    id(BuildPlugins.NAVIGATION_SAFE_ARGS)
    id(BuildPlugins.FIREBASE_CRASHLYTICS)
    id(BuildPlugins.GOOGLE_SERVICES)
}

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        applicationId = BuildAndroidConfig.APP_ID
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(BuildAndroidConfig.TARGET_SDK_VERSION)

        versionCode = BuildAndroidConfig.VERSION_CODE
        versionName = BuildAndroidConfig.VERSION_NAME

        testInstrumentationRunner = BuildAndroidConfig.TEST_INSTRUMENTATION_RUNNER

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")

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
        freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    hilt {
        enableTransformForLocalTests = true
    }
}

dependencies {

    implementation(project(BuildModules.CORE))
    implementation(project(BuildModules.DESIGN))
    implementation(project(BuildModules.TRANSLATIONS))

    implementation(Dependencies.Kotlin.KOTLIN)

    implementation(Dependencies.MATERIAL)
    implementation(Dependencies.AndroidX.CORE)
    implementation(Dependencies.AndroidX.APPCOMPAT)
    implementation(Dependencies.AndroidX.CONSTRAINT_LAYOUT)
    implementation(Dependencies.AndroidX.FRAGMENT_KTX)
    implementation(Dependencies.AndroidX.NAVIGATION_UI)
    implementation(Dependencies.AndroidX.NAVIGATION_FRAGMENT)
    implementation(Dependencies.AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(Dependencies.AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(Dependencies.AndroidX.LIFECYCLE_LIVEDATA)
    implementation(Dependencies.AndroidX.RECYCLER_VIEW)
    implementation(Dependencies.AndroidX.START_UP)
    implementation(Dependencies.AndroidX.HILT_VIEWMODEL)

    implementation(Dependencies.HILT)
    kapt(Dependencies.HILT_COMPILER)
    kapt(Dependencies.AndroidX.HILT_COMPILER)

    implementation(Dependencies.Kotlin.COROUTINES)
    implementation(Dependencies.Kotlin.COROUTINES_ANDROID)

    implementation(platform(Dependencies.Firebase.BOM))
    implementation(Dependencies.Firebase.CORE)
    implementation(Dependencies.Firebase.ANALYTICS)
    implementation(Dependencies.Firebase.AUTH)
    implementation(Dependencies.Firebase.CRASHLYTICS)

    implementation(Dependencies.TIMBER)

    addTestsDependencies()
}