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
import dependencies.Dependencies.Accompanist
import dependencies.Dependencies.AndroidX
import dependencies.Dependencies.Coil
import dependencies.Dependencies.Compose
import dependencies.Dependencies.Hilt
import dependencies.Dependencies.Iconics
import dependencies.Dependencies.Kotlin
import dependencies.Versions
import extensions.addTestsDependencies
import extensions.kapt

plugins {
    id(BuildPlugins.Android.LIBRARY)
    id(BuildPlugins.Kotlin.ANDROID)
    id(BuildPlugins.Kotlin.KAPT)
    id(BuildPlugins.DAGGER_HILT)
    id(BuildPlugins.Kotlin.PARCELIZE)
}

android {
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
    }

    compileOptions {
        sourceCompatibility = JavaOptions.version
        targetCompatibility = JavaOptions.version
    }
    kotlinOptions {
        jvmTarget = JavaOptions.version.toString()
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.COROUTINES
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.OPT_IN
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.COMPOSE
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    implementation(project(BuildModules.Common.AUTH))
    implementation(project(BuildModules.Common.CORE))
    implementation(project(BuildModules.Common.DESIGN))
    implementation(project(BuildModules.Common.TRANSLATIONS))
    implementation(project(BuildModules.Common.LESSONS_DATA))
    implementation(project(BuildModules.Features.APP_WIDGETS))
    implementation(project(BuildModules.Features.BIBLE))
    implementation(project(BuildModules.Features.READER))
    implementation(project(BuildModules.Features.MEDIA))
    implementation(project(BuildModules.Features.PDF))

    implementation(Kotlin.COROUTINES)
    implementation(Kotlin.COROUTINES_ANDROID)

    implementation(Dependencies.MATERIAL)
    implementation(AndroidX.CORE)
    implementation(AndroidX.APPCOMPAT)
    implementation(AndroidX.CONSTRAINT_LAYOUT)
    implementation(AndroidX.ACTIVITY)
    implementation(AndroidX.FRAGMENT_KTX)
    implementation(AndroidX.LIFECYCLE_VIEWMODEL)
    implementation(AndroidX.LIFECYCLE_EXTENSIONS)
    implementation(AndroidX.RECYCLER_VIEW)

    implementation(Hilt.ANDROID)
    kapt(Hilt.COMPILER)

    implementation(Dependencies.JODA)
    implementation(Dependencies.TIMBER)
    implementation(Coil.core)
    implementation(Coil.compose)
    implementation(Dependencies.TAP_TARGET)
    implementation(Iconics.CORE)
    implementation(Iconics.VIEWS)
    implementation(Iconics.TYPEFACE)
    implementation(Iconics.MATERIAL_TYPEFACE)
    implementation("com.github.hotchemi:android-rate:1.0.1")
    implementation("com.afollestad.material-dialogs:input:3.3.0")
    implementation(Dependencies.Cascade)
    implementation(Dependencies.Facebook.SHIMMER)
    implementation(Dependencies.MarkWorm.core)

    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.tooling)

    implementation(Accompanist.placeholder)

    addTestsDependencies()
    testImplementation(project(BuildModules.Libraries.TEST_UTILS))
}
