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
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android { namespace = "io.adventech.blockkit.ui" }

foundry {
    features { compose() }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    api(projects.libraries.blockKit.model)
    api(projects.libraries.blockKit.parser)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.atlassian.commonmark)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.compose.tooling)
    implementation(libs.coil.compose)
    implementation(libs.google.hilt.android)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.saket.extendedspans)
    implementation(libs.saket.telephoto)
    implementation(libs.saket.telephoto.flick)
    implementation(libs.timber)
    implementation(projects.common.translations)
    implementation(projects.libraries.cascadeCompose)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.media.api)
    implementation(projects.libraries.media.resources)
    implementation(projects.libraries.ui.placeholder)
    implementation(projects.services.media.ui)

    testImplementation(libs.bundles.testing.common)

    ksp(libs.circuit.codegen)
    ksp(libs.google.hilt.compiler)
}
