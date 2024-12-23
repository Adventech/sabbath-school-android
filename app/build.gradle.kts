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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.foundry.base)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.hilt)
}

val useReleaseKeystore = file(BuildAndroidConfig.KEYSTORE_PROPS_FILE).exists()
val appVersionCode = readPropertyValue(
    filePath = BuildAndroidConfig.VERSION_PROPS_FILE,
    key = "BUILD_NUMBER",
    defaultValue = "1"
).toInt() + 21856

val webClientId = readPropertyValue(
    filePath = "$rootDir/${BuildAndroidConfig.API_KEYS_PROPS_FILE}",
    key = "WEB_CLIENT_ID",
    defaultValue = ""
)

android {
    namespace = BuildAndroidConfig.APP_ID

    defaultConfig {
        applicationId = BuildAndroidConfig.APP_ID

        versionCode = appVersionCode
        versionName = "${BuildAndroidConfig.Version.name} ($appVersionCode)"

        testInstrumentationRunner = "com.cryart.sabbathschool.SSAppTestRunner"

        ndk {
            abiFilters.addAll(
                // keep all binaries supported by PSPDFKit
                listOf("x86", "x86_64", "arm64-v8a", "armeabi-v7a")
            )
        }

        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
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
        val release by getting {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
            if (useReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }

            ndk { debugSymbolLevel = "FULL" }
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        resources.excludes += "/META-INF/AL2.0"
        resources.excludes += "/META-INF/LGPL2.1"
    }
}

foundry {
    features { compose() }
    android { features { androidTest() } }
}

ksp {
    arg("circuit.codegen.mode", "hilt")
}

dependencies {
    baselineProfile(projects.baselineprofile)

    implementation(projects.common.auth)
    implementation(projects.common.core)
    implementation(projects.common.design)
    implementation(projects.common.designCompose)
    implementation(projects.common.lessonsData)
    implementation(projects.common.network)
    implementation(projects.common.runtimePermissions)
    implementation(projects.common.translations)
    implementation(projects.features.appWidgets)
    implementation(projects.features.auth)
    implementation(projects.features.bible)
    implementation(projects.features.document)
    implementation(projects.features.feed)
    implementation(projects.features.languages)
    implementation(projects.features.lessons)
    implementation(projects.features.media)
    implementation(projects.features.navigationSuite)
    implementation(projects.features.pdf)
    implementation(projects.features.quarterlies)
    implementation(projects.features.readings)
    implementation(projects.features.resource)
    implementation(projects.features.settings)
    implementation(projects.libraries.circuit.api)
    implementation(projects.libraries.foundation.coroutines)
    implementation(projects.services.circuit.impl)
    implementation(projects.services.lessons.impl)
    implementation(projects.services.media.impl)
    implementation(projects.services.prefs.impl)
    implementation(projects.services.resources.impl)
    implementation(projects.services.storage.impl)
    implementation(projects.services.workers.impl)

    implementation(libs.google.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.work)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    ksp(libs.circuit.codegen)

    implementation(libs.timber)

    implementation(libs.joda.android)

    testImplementation(libs.bundles.testing.common)
    testImplementation(projects.libraries.foundation.coroutines.test)
    testImplementation(projects.libraries.testUtils)
    kspTest(libs.google.hilt.compiler)
    androidTestImplementation(libs.bundles.testing.android.common)
    kspAndroidTest(libs.google.hilt.compiler)
    androidTestImplementation(projects.libraries.testUtils)
    androidTestImplementation(libs.test.androidx.espresso.contrib) {
        exclude(group = "org.checkerframework", module = "checker")
    }
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

object BuildAndroidConfig {
    const val APP_ID = "com.cryart.sabbathschool"
    const val KEYSTORE_PROPS_FILE = "../release/keystore.properties"
    const val API_KEYS_PROPS_FILE = "release/ss_public_keys.properties"
    const val VERSION_PROPS_FILE = "../release/build_number.properties"

    object Version {
        private const val MAJOR = 5
        private const val MINOR = 0
        private const val PATCH = 0

        const val name = "$MAJOR.$MINOR.$PATCH"
    }
}
