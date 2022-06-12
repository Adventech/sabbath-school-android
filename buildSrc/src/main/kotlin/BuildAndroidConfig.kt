object BuildAndroidConfig {
    const val APP_ID = "com.cryart.sabbathschool"
    const val KEYSTORE_PROPS_FILE = "../release/keystore.properties"
    const val API_KEYS_PROPS_FILE = "release/ss_public_keys.properties"

    const val COMPILE_SDK_VERSION = 32
    const val MIN_SDK_VERSION = 21
    const val TARGET_SDK_VERSION = 32

    const val TEST_INSTRUMENTATION_RUNNER = "com.cryart.sabbathschool.SSAppTestRunner"

    object Version {
        private const val MAJOR = 4
        private const val MINOR = 10
        private const val PATCH = 3

        const val name = "$MAJOR.$MINOR.$PATCH"
    }
}
