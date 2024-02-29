/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

plugins {
    alias(libs.plugins.sgp.base)
    alias(libs.plugins.ksp)
    id("com.android.library")
    id("dagger.hilt.android.plugin")
    kotlin("android")
}

android {
    namespace = "com.cryart.sabbathschool.account"

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.common.auth)
    implementation(projects.common.core)
    implementation(projects.common.design)
    implementation(projects.common.lessonsData)
    implementation(projects.common.models)
    implementation(projects.common.translations)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.foundation.coroutines)

    implementation(libs.google.material)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)

    implementation(libs.google.play.auth)

    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)

    implementation(libs.kotlin.coroutines.playservices)

    implementation(libs.coil.core)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)
}
