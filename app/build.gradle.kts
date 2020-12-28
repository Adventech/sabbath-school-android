import dependencies.Dependencies
import extensions.addTestsDependencies

plugins {
    id(BuildPlugins.ANDROID_APPLICATION)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
    id(BuildPlugins.DAGGER_HILT)
    id(BuildPlugins.NAVIGATION_SAFE_ARGS)
}

android {
    compileSdkVersion(BuildAndroidConfig.COMPILE_SDK_VERSION)
    buildToolsVersion(BuildAndroidConfig.BUILD_TOOLS_VERSION)

    defaultConfig {
        applicationId = BuildAndroidConfig.APP_ID
        minSdkVersion(BuildAndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(BuildAndroidConfig.TARGET_SDK_VERSION)

        versionCode = BuildAndroidConfig.VERSION_CODE
        versionName = BuildAndroidConfig.VERSION_NAME

        testInstrumentationRunner = BuildAndroidConfig.TEST_INSTRUMENTATION_RUNNER

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {

    implementation(project(BuildModules.DESIGN))

    implementation(Dependencies.KOTLIN)
    implementation(Dependencies.CORE_KTX)
    implementation(Dependencies.APPCOMPAT)
    implementation(Dependencies.MATERIAL)
    implementation(Dependencies.CONSTRAINT_LAYOUT)
    implementation(Dependencies.FRAGMENT_KTX)
    implementation(Dependencies.NAVIGATION_UI)
    implementation(Dependencies.NAVIGATION_FRAGMENT)
    implementation(Dependencies.LIFECYCLE_VIEWMODEL)
    implementation(Dependencies.LIFECYCLE_EXTENSIONS)
    implementation(Dependencies.RECYCLER_VIEW)
    implementation(Dependencies.HILT)
    kapt(Dependencies.HILT_COMPILER)
    implementation(Dependencies.COROUTINES)
    implementation(Dependencies.COROUTINES_ANDROID)
    implementation(Dependencies.TIMBER)

    addTestsDependencies()
}