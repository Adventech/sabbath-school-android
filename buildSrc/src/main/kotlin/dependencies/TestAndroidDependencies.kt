
package dependencies

object TestAndroidDependencies {
    const val RUNNER = "androidx.test:runner:${Versions.ANDROIDX_TEST}"
    const val RULES = "androidx.test:rules:${Versions.ANDROIDX_TEST}"
    const val JUNIT = "androidx.test.ext:junit:${Versions.EXT}"

    object Espresso {
        const val core = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO}"
        const val contrib = "androidx.test.espresso:espresso-contrib:${Versions.ESPRESSO}"
        const val intents = "androidx.test.espresso:espresso-intents:${Versions.ESPRESSO}"
    }
}
