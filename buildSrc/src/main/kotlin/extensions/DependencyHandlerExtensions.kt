/*
 * Copyright 2019 vmadalin.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package extensions

import dependencies.TestAndroidDependencies
import dependencies.TestDependencies
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Adds a dependency to the `debugImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.debugImplementation(dependencyNotation: String): Dependency? =
    add("debugImplementation", dependencyNotation)

/**
 * Adds a dependency to the `implementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.implementation(dependencyNotation: String): Dependency? =
    add("implementation", dependencyNotation)

/**
 * Adds a dependency to the `api` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.api(dependencyNotation: String): Dependency? =
    add("api", dependencyNotation)

/**
 * Adds a dependency to the `kapt` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.kapt(dependencyNotation: String): Dependency? =
    add("kapt", dependencyNotation)

/**
 * Adds a dependency to the `kaptTest` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.kaptTest(dependencyNotation: String): Dependency? =
    add("kaptTest", dependencyNotation)

/**
 * Adds a dependency to the `testImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.testImplementation(dependencyNotation: String): Dependency? =
    add("testImplementation", dependencyNotation)


/**
 * Adds a dependency to the `androidTestImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.androidTestImplementation(dependencyNotation: String): Dependency? =
    add("androidTestImplementation", dependencyNotation)

/**
 * Adds a dependency to the `kaptAndroidTest` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.kaptAndroidTest(dependencyNotation: String): Dependency? =
    add("kaptAndroidTest", dependencyNotation)

/**
 * Adds all the tests dependencies to specific configuration.
 */
fun DependencyHandler.addTestsDependencies() {
    testImplementation(TestDependencies.JUNIT)
    testImplementation(TestDependencies.MOCKK)
    testImplementation(TestDependencies.ROBOELECTRIC)
    testImplementation(TestDependencies.CORE)
    testImplementation(TestDependencies.ARCH_CORE)
    testImplementation(TestDependencies.RULES)
    testImplementation(TestDependencies.RUNNER)
    testImplementation(TestDependencies.EXT)
    testImplementation(TestDependencies.COROUTINES_TEST)
    testImplementation(TestDependencies.FRAGMENT_TEST)
    testImplementation(TestDependencies.KLUENT)
    testImplementation(TestDependencies.HILT)
    testImplementation(TestDependencies.JODA)
    testImplementation(TestDependencies.TURBINE)
    kaptTest(TestDependencies.HILT_COMPILER)

    androidTestImplementation(TestAndroidDependencies.Espresso.core)
    androidTestImplementation(TestAndroidDependencies.Espresso.contrib)
    androidTestImplementation(TestAndroidDependencies.Espresso.intents)
    androidTestImplementation(TestAndroidDependencies.RUNNER)
    androidTestImplementation(TestAndroidDependencies.RULES)
    androidTestImplementation(TestAndroidDependencies.JUNIT)
    androidTestImplementation(TestAndroidDependencies.FRAGMENT_TEST)
    androidTestImplementation(TestDependencies.COROUTINES_TEST)
    androidTestImplementation(TestDependencies.ARCH_CORE)
    androidTestImplementation(TestDependencies.HILT)
    androidTestImplementation(TestAndroidDependencies.Compose.junit)
    debugImplementation(TestAndroidDependencies.Compose.manifest)
    kaptAndroidTest(TestDependencies.HILT_COMPILER)
}
