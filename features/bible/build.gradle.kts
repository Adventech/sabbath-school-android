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
import dependencies.Dependencies.AndroidX
import dependencies.Dependencies.Firebase
import dependencies.Dependencies.Kotlin
import dependencies.Dependencies.Hilt
import dependencies.Dependencies.Iconics
import extensions.addTestsDependencies
import extensions.kapt

plugins {
    id(BuildPlugins.Android.LIBRARY)
    id(BuildPlugins.Kotlin.ANDROID)
    id(BuildPlugins.Kotlin.KAPT)
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
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.COROUTINES
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(project(BuildModules.Common.CORE))
    implementation(project(BuildModules.Common.DESIGN))
    implementation(project(BuildModules.Common.TRANSLATIONS))
    implementation(project(BuildModules.Common.LESSONS_DATA))
    implementation(project(BuildModules.Features.READER))

    implementation(Kotlin.COROUTINES)
    implementation(Kotlin.COROUTINES_ANDROID)

    implementation(Dependencies.MATERIAL)
    implementation(AndroidX.CORE)
    implementation(AndroidX.APPCOMPAT)
    implementation(AndroidX.CONSTRAINT_LAYOUT)
    implementation(AndroidX.ACTIVITY)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL)

    implementation(Hilt.ANDROID)
    kapt(Hilt.COMPILER)

    implementation(Dependencies.TIMBER)
    implementation(Iconics.VIEWS)

    addTestsDependencies()
}
