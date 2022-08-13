
package plugins

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.46.1")
}

tasks {
    register<JavaExec>("ktlint") {
        group = "verification"
        description = "Check Kotlin code style."
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        args("--android", "src/**/*.kt")
    }

    register<JavaExec>("ktlintFormat") {
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        args("--android", "-F", "src/**/*.kt")
    }
}

configurations.named("ktlint").configure {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("com.pinterest:ktlint")).using(variant(module("com.pinterest:ktlint:0.46.1")) {
                attributes {
                    attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class, Bundling.EXTERNAL))
                }
            })
        }
    }
}
