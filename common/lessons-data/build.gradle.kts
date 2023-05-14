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

plugins {
    alias(libs.plugins.sgp.base)
    alias(libs.plugins.ksp)
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "app.ss.lessons.data"

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

dependencies {
    implementation(projects.common.auth)
    implementation(projects.common.core)
    api(projects.common.models)
    implementation(projects.common.network)
    implementation(projects.common.storage)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.google.hilt.android)
    kapt(libs.google.hilt.compiler)
    implementation(libs.timber)
    implementation(libs.joda.android)

    implementation(libs.square.moshi.kotlin)
    ksp(libs.square.moshi.codegen)
    compileOnly(libs.javax.annotation)

    api(libs.square.okhttp)
    implementation(libs.square.okhttp.logging)

    implementation(libs.square.retrofit)
    implementation(libs.square.retrofit.converter.moshi)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.testUtils)
}
