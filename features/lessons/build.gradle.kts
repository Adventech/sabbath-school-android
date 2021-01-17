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
import extensions.kapt

plugins {
    id(BuildPlugins.ANDROID_LIBRARY)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
    id(BuildPlugins.DAGGER_HILT)
}

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)

    defaultConfig {
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
    implementation(Dependencies.AndroidX.ACTIVITY)
    implementation(Dependencies.AndroidX.FRAGMENT_KTX)
    implementation(Dependencies.AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(Dependencies.AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(Dependencies.AndroidX.LIFECYCLE_LIVEDATA)
    implementation(Dependencies.AndroidX.RECYCLER_VIEW)
    implementation(Dependencies.AndroidX.HILT_VIEWMODEL)

    implementation(Dependencies.HILT)
    kapt(Dependencies.HILT_COMPILER)
    kapt(Dependencies.AndroidX.HILT_COMPILER)

    implementation(platform(Dependencies.Firebase.BOM))
    implementation(Dependencies.Firebase.DATABASE)
    implementation(Dependencies.Firebase.ANALYTICS)
    implementation(Dependencies.Firebase.AUTH)

    implementation(Dependencies.Kotlin.COROUTINES)
    implementation(Dependencies.Kotlin.COROUTINES_ANDROID)
    implementation(Dependencies.Kotlin.COROUTINES_PLAY_SERVICES)

    implementation(Dependencies.JODA)
    implementation(Dependencies.TIMBER)
    implementation(Dependencies.COIL)
    implementation(Dependencies.TAP_TARGET)
    implementation("com.mikepenz:iconics-core:2.8.1@aar")
    implementation("com.mikepenz:google-material-typeface:2.2.0.3.original@aar")
}