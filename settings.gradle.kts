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

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://customers.pspdfkit.com/maven")
    }
}

include(
    ":app",
    ":app-tv",
    ":baselineprofile",
    ":common:auth",
    ":common:core",
    ":common:design",
    ":common:design-compose",
    ":features:languages",
    ":common:misc",
    ":common:models",
    ":common:network",
    ":common:runtime-permissions",
    ":common:translations",
    ":features:app-widgets",
    ":features:auth",
    ":features:document",
    ":features:feed",
    ":features:media",
    ":features:navigation-suite",
    ":features:pdf",
    ":features:resource",
    ":features:settings",
    ":features:share",
    ":libraries:app-widget:api",
    ":libraries:block-kit:model",
    ":libraries:block-kit:parser",
    ":libraries:block-kit:ui",
    ":libraries:cascade-compose",
    ":libraries:circuit:api",
    ":libraries:foundation:android",
    ":libraries:foundation:coroutines",
    ":libraries:foundation:coroutines:test",
    ":libraries:lessons:api",
    ":libraries:lessons:model",
    ":libraries:lessons:test",
    ":libraries:media:api",
    ":libraries:media:model",
    ":libraries:media:resources",
    ":libraries:media:service",
    ":libraries:media:test-fixtures",
    ":libraries:pdf:api",
    ":libraries:prefs:api",
    ":libraries:prefs:model",
    ":libraries:storage:api",
    ":libraries:storage:test",
    ":libraries:test_utils:robolectric-core",
    ":libraries:test_utils:roborazzi",
    ":libraries:ui:placeholder",
    ":services:auth:overlay",
    ":services:circuit:impl",
    ":services:lessons:impl",
    ":services:media:impl",
    ":services:media:ui",
    ":services:prefs:impl",
    ":services:resources:api",
    ":services:resources:impl",
    ":services:resources:model",
    ":services:storage:impl",
)
rootProject.name = "sabbath-school-android"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
