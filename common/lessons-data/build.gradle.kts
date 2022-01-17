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
import dependencies.Dependencies.Firebase
import dependencies.Dependencies.Kotlin
import dependencies.Dependencies.Hilt
import dependencies.Dependencies.Square.Moshi
import dependencies.Dependencies.Square.Okhttp
import dependencies.Dependencies.Square.Retrofit
import extensions.addTestsDependencies
import extensions.api
import extensions.implementation
import extensions.kapt

plugins {
    id(BuildPlugins.Android.LIBRARY)
    id(BuildPlugins.Kotlin.ANDROID)
    id(BuildPlugins.Kotlin.KAPT)
    id(BuildPlugins.Kotlin.PARCELIZE)
    id(BuildPlugins.DAGGER_HILT)
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
    }
}

dependencies {
    implementation(project(BuildModules.Common.CORE))
    implementation(project(BuildModules.Common.STORAGE))

    implementation(Kotlin.COROUTINES)
    implementation(Kotlin.COROUTINES_ANDROID)
    implementation(Hilt.ANDROID)
    kapt(Hilt.COMPILER)
    implementation(Dependencies.TIMBER)
    implementation(Dependencies.JODA)

    implementation(platform(Firebase.BOM))
    implementation(Firebase.DATABASE)
    implementation(Firebase.AUTH)

    implementation(Moshi.kotlin)
    kapt(Moshi.codegen)

    api(Okhttp.okhttp)
    implementation(Okhttp.logging)

    implementation(Retrofit.retrofit2)
    implementation(Retrofit.moshiConverter)

    addTestsDependencies()
    testImplementation(project(BuildModules.Libraries.TEST_UTILS))
}
