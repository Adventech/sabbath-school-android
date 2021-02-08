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
import extensions.addTestsDependencies
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
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.COROUTINES
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(project(BuildModules.Common.CORE))
    implementation(project(BuildModules.Common.DESIGN))
    implementation(project(BuildModules.Common.TRANSLATIONS))
    implementation(project(BuildModules.Features.READER))
    implementation(project(BuildModules.Features.BIBLE))

    implementation(Kotlin.KOTLIN)
    implementation(Kotlin.COROUTINES)
    implementation(Kotlin.COROUTINES_ANDROID)
    implementation(Kotlin.COROUTINES_PLAY_SERVICES)

    implementation(Dependencies.MATERIAL)
    implementation(AndroidX.CORE)
    implementation(AndroidX.APPCOMPAT)
    implementation(AndroidX.CONSTRAINT_LAYOUT)
    implementation(AndroidX.ACTIVITY)
    implementation(AndroidX.FRAGMENT_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(AndroidX.LIFECYCLE_LIVEDATA)
    implementation(AndroidX.RECYCLER_VIEW)

    implementation(Hilt.ANDROID)
    kapt(Hilt.COMPILER)

    implementation(platform(Firebase.BOM))
    implementation(Firebase.DATABASE)
    implementation(Firebase.STORAGE)
    implementation(Firebase.ANALYTICS)
    implementation(Firebase.AUTH)

    implementation(Dependencies.JODA)
    implementation(Dependencies.TIMBER)
    implementation(Dependencies.COIL)
    implementation(Dependencies.TAP_TARGET)
    implementation("com.mikepenz:iconics-core:5.2.4@aar")
    implementation("com.mikepenz:iconics-views:5.2.4@aar")
    implementation("com.mikepenz:iconics-typeface-api:5.2.6@aar")
    implementation("com.mikepenz:google-material-typeface:4.0.0.1-kotlin@aar")
    implementation("net.opacapp:multiline-collapsingtoolbar:27.1.1")
    implementation("com.github.hotchemi:android-rate:1.0.1")
    implementation("com.afollestad.material-dialogs:core:0.9.6.0")
    implementation("ru.beryukhov:flowreactivenetwork:1.0.2")

    addTestsDependencies()
}
