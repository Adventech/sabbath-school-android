import org.gradle.api.JavaVersion

interface BuildType {

    companion object {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}

object KotlinOptions {
    const val COROUTINES = "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
    const val OPT_IN = "-Xopt-in=kotlin.RequiresOptIn"
}

object JavaOptions {
    val version = JavaVersion.VERSION_11
}
