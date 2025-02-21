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
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
}

android { namespace = "app.ss.media" }

foundry {
    features { compose() }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(projects.common.core)
    implementation(projects.common.design)
    implementation(projects.common.designCompose)
    implementation(projects.common.translations)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.media.api)
    implementation(projects.libraries.media.resources)
    implementation(projects.libraries.media.service)
    implementation(projects.libraries.storage.api)
    implementation(projects.libraries.circuit.api)
    implementation(projects.services.media.ui)
    implementation(projects.services.resources.api)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.extensions)

    implementation(libs.google.hilt.android)
    implementation(libs.google.material)

    implementation(libs.kotlin.coroutines.guava)
    implementation(libs.timber)
    implementation(libs.coil.compose)

    ksp(libs.circuit.codegen)
    ksp(libs.google.hilt.compiler)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.testUtils)
}
