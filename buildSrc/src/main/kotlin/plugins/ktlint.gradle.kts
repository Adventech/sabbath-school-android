
package plugins
import dependencies.PluginVersions

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:${PluginVersions.KTLINT}")
}

tasks {
    register<JavaExec>("ktlint") {
        group = BuildTasksGroups.VERIFICATION
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("--android", "src/**/*.kt")
    }

    register<JavaExec>("ktlintFormat") {
        group = BuildTasksGroups.FORMATTING
        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("--android", "-F", "src/**/*.kt")
    }
}
