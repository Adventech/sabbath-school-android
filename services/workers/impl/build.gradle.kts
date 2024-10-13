/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(projects.common.design)
    implementation(projects.common.translations)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.lessons.api)
    implementation(projects.libraries.storage.api)
    api(projects.libraries.workers.api)

    implementation(libs.androidx.work)
    implementation(libs.coil.core)
    implementation(libs.androidx.hilt.work)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.kotlin.coroutines)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing.common)
    testImplementation(libs.test.androidx.work)
    testImplementation(projects.libraries.foundation.coroutines.test)
    testImplementation(projects.libraries.lessons.test)
    testImplementation(projects.libraries.storage.test)
}
