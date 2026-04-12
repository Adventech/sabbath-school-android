/*
 * Copyright (c) 2026. Adventech <info@adventech.io>
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

import com.android.build.api.dsl.LibraryExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

val psPdfKitKey = readPropertyValue(
    filePath = "$rootDir/${BuildAndroidConfig.API_KEYS_PROPS_FILE}",
    key = "PSPDFKIT_LICENSE",
    defaultValue = ""
)

extensions.configure<LibraryExtension> {
    namespace = "ss.services.pdf.impl"

    defaultConfig {
        manifestPlaceholders["psPdfKitKey"] = psPdfKitKey
    }
}

dependencies {
    implementation(libs.androidx.datastore.prefs)
    implementation(libs.androidx.preference)
    implementation(libs.google.hilt.android)
    implementation(libs.nutrient)
    implementation(libs.timber)
    implementation(projects.common.models)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.pdf.api)

    ksp(libs.google.hilt.compiler)
}

/**
 * Reads a value saved in a [Properties] file
 */
fun Project.readPropertyValue(
    filePath: String,
    key: String,
    defaultValue: String
): String {
    val file = file(filePath)
    return if (file.exists()) {
        val keyProps = Properties().apply {
            load(FileInputStream(file))
        }
        keyProps.getProperty(key, defaultValue)
    } else {
        defaultValue
    }
}

object BuildAndroidConfig {
    const val API_KEYS_PROPS_FILE = "release/ss_public_keys.properties"
}
