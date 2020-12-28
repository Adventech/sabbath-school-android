package plugins

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

apply<VersionsPlugin>()

tasks {
    withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                        .any { it.matches(candidate.version) }

                    if (rejected) {
                        reject("Release candidate")
                    }
                }
            }
        }
    }
}