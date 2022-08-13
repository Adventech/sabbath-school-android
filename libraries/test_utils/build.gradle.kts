/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

dependencies {
    implementation(project(":common:auth"))
    implementation(project(":common:core"))
    implementation(project(":common:lessons-data"))

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)

    implementation(libs.google.hilt.android)
    implementation(libs.test.google.hilt)
    kapt(libs.google.hilt.compiler)

    implementation(libs.timber)

    implementation(libs.square.moshi.kotlin)
    kapt(libs.square.moshi.codegen)

    implementation(libs.test.junit)
    implementation(libs.test.coroutines)

    kaptTest(libs.square.moshi.codegen)
}
