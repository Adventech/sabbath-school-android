/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
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
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
}

android { namespace = "app.ss.languages" }

foundry {
    features { compose() }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(projects.common.designCompose)
    implementation(projects.common.translations)
    implementation(projects.libraries.appWidget.api)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.prefs.api)
    implementation(projects.services.resources.api)

    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    ksp(libs.circuit.codegen)
    implementation(libs.kotlinx.collectionsImmutable)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)
}
