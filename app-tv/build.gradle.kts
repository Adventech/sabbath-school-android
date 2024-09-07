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

import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.sgp.base)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    id("dagger.hilt.android.plugin")
}

val useReleaseKeystore = file(BuildAndroidConfig.KEYSTORE_PROPS_FILE).exists()
val appVersionCode = readPropertyValue(
    filePath = BuildAndroidConfig.VERSION_PROPS_FILE,
    key = "BUILD_NUMBER",
    defaultValue = "1"
).toInt() + 12500

android {
    namespace = "app.ss.tv"

    defaultConfig {
        applicationId = BuildAndroidConfig.APP_ID
        versionCode = appVersionCode
        versionName = "0.8.0 - $appVersionCode"
        minSdk = 25
    }

    signingConfigs {
        if (useReleaseKeystore) {
            val keyProps = Properties().apply {
                load(FileInputStream(file(BuildAndroidConfig.KEYSTORE_PROPS_FILE)))
            }

            create("release") {
                storeFile = file(keyProps.getProperty("release.keystore"))
                storePassword = keyProps.getProperty("release.keystore.password")
                keyAlias = keyProps.getProperty("key.alias")
                keyPassword = keyProps.getProperty("key.password")
            }
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
            if (useReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=androidx.tv.foundation.ExperimentalTvFoundationApi"
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

slack {
    features { compose() }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    implementation(projects.common.translations)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.libraries.lessons.api)
    implementation(projects.libraries.media.api)
    implementation(projects.libraries.media.resources)
    implementation(projects.libraries.media.service)
    implementation(projects.services.lessons.impl)
    implementation(projects.services.media.impl)
    implementation(projects.services.prefs.impl)
    implementation(projects.services.storage.impl)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.compose.tooling)
    implementation(libs.androidx.core)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material) {
        exclude(group = "androidx.compose.animation", module = "animation")
    }
    implementation(libs.bundles.circuit)
    implementation(libs.coil.compose)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    ksp(libs.circuit.codegen)
    implementation(libs.kotlinx.collectionsImmutable)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)
    testImplementation(projects.libraries.lessons.test)
    testImplementation(projects.libraries.media.testFixtures)
    testImplementation(projects.libraries.storage.test)
    testImplementation(projects.libraries.testUtils)
}

object BuildAndroidConfig {
    const val APP_ID = "com.cryart.sabbathschool"
    const val KEYSTORE_PROPS_FILE = "../release/keystore.properties"
    const val VERSION_PROPS_FILE = "../release/build_number.properties"
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
        return keyProps.getProperty(key, defaultValue)
    } else {
        defaultValue
    }
}
