package dependencies

object TestDependencies {

    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
    const val ROBOELECTRIC = "org.robolectric:robolectric:${Versions.ROBOELECTRIC}"
    const val CORE = "androidx.test:core:${Versions.ANDROIDX_TEST}"
    const val RUNNER = "androidx.test:runner:${Versions.ANDROIDX_TEST}"
    const val RULES = "androidx.test:rules:${Versions.ANDROIDX_TEST}"
    const val EXT = "androidx.test.ext:junit:${Versions.EXT}"
    const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}"
    const val ARCH_CORE = "androidx.arch.core:core-testing:${Versions.ARCH_CORE}"
    const val FRAGMENT_TEST = "androidx.fragment:fragment-testing:${Versions.FRAGMENT_TEST}"
    const val KLUENT = "org.amshove.kluent:kluent-android:${Versions.KLUENT}"
    const val HILT = "com.google.dagger:hilt-android-testing:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"
}