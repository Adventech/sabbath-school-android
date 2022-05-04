import org.gradle.api.JavaVersion

interface BuildType {

    companion object {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}

object KotlinOptions {
    const val COROUTINES = "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    const val OPT_IN = "-Xopt-in=kotlin.RequiresOptIn"

    object Compose {
        const val ReportsDestination = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination"
        const val MetricsDestination = "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination"
    }
}

object JavaOptions {
    val version = JavaVersion.VERSION_11
}
