object BuildAndroidConfig {
    const val APP_ID = "com.cryart.sabbathschool"
    const val KEYSTORE_PROPS_FILE = "../release/keystore.properties"

    const val COMPILE_SDK_VERSION = 30
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 30

    const val TEST_INSTRUMENTATION_RUNNER = "com.cryart.sabbathschool.SSAppTestRunner"

    object Version {
        private const val MAJOR = 4
        private const val MINOR = 1
        private const val PATCH = 1

        const val name = "$MAJOR.$MINOR.$PATCH"
    }
}
