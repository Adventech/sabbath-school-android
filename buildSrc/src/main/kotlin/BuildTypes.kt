
interface BuildType {

    companion object {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}

object KotlinOptions {
    const val COROUTINES = "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
}