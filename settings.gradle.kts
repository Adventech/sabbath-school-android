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
    ":benchmark",
    ":common:auth",
    ":common:core",
    ":common:design",
    ":common:design-compose",
    ":features:languages",
    ":common:lessons-data",
    ":common:misc",
    ":common:models",
    ":common:network",
    ":common:prefs:api",
    ":common:prefs:impl",
    ":common:prefs:model",
    ":common:runtime-permissions",
    ":common:storage",
    ":common:storage:test",
    ":common:translations",
    ":features:account",
    ":features:app-widgets",
    ":features:bible",
    ":features:lessons",
    ":features:lessons:intro",
    ":features:media",
    ":features:pdf",
    ":features:reader",
    ":features:settings",
    ":libraries:circuit-helpers:api",
    ":libraries:circuit-helpers:impl",
    ":libraries:foundation:android",
    ":libraries:foundation:coroutines",
    ":libraries:foundation:coroutines:test",
    ":libraries:lessons:api",
    ":libraries:lessons:model",
    ":libraries:lessons:test",
    ":libraries:media:api",
    ":libraries:media:model",
    ":libraries:test_utils",
    ":libraries:ui:placeholder",
    ":libraries:workers:api",
    ":services:lessons:impl",
    ":services:media:impl",
    ":services:workers:impl"
)
rootProject.name = "sabbath-school-android"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
