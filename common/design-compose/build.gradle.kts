/*
 * Copyright (c) 2022. Adventech <info@adventech.io>
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
import dependencies.Dependencies.Kotlin
import dependencies.Versions

plugins {
    id(BuildPlugins.Android.LIBRARY)
    id(BuildPlugins.Kotlin.ANDROID)
    id(BuildPlugins.PAPARAZZI)
}

android {
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.COMPOSE
    }
    kotlinOptions {
        jvmTarget = JavaOptions.version.toString()
        freeCompilerArgs = freeCompilerArgs + KotlinOptions.OPT_IN
    }
    compileOptions {
        sourceCompatibility = JavaOptions.version
        targetCompatibility = JavaOptions.version
    }
}

dependencies {
    implementation(project(BuildModules.Common.TRANSLATIONS))

    implementation(Dependencies.TIMBER)
    implementation(Accompanist.placeholder)
    implementation(AndroidX.LIFECYCLE_KTX)
    implementation(Coil.compose)
    implementation(Kotlin.COROUTINES)

    implementation(Compose.material)
    implementation(Compose.snapper)

    api(Compose.foundation)
    api(Compose.foundationLayout)
    api(Compose.ui)
    api(Compose.uiUtil)
    api(Compose.runtime)
    api(Compose.material3)
    api(Compose.toolingPreview)
    api(Compose.icons)
    api(Compose.iconsExtended)
    api(Compose.windowSizeClass)
    debugApi(Compose.tooling)
    debugApi(Compose.Preview.customView)
    debugApi(Compose.Preview.customViewContainer)
    debugApi(Compose.Preview.viewModel)
    debugApi(Compose.Preview.savedState)
}
