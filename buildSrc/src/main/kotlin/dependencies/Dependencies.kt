package dependencies

object Dependencies {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
    
    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    const val APPCOMPAT = "androidx.appcompat:appcompat:${Versions.APPCOMPAT}"
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"
    const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.RECYCLER_VIEW}"
    const val NAVIGATION_FRAGMENT = "androidx.navigation:navigation-fragment-ktx:${Versions.NAVIGATION}"
    const val NAVIGATION_UI = "androidx.navigation:navigation-ui-ktx:${Versions.NAVIGATION}"
    const val LIFECYCLE_EXTENSIONS = "androidx.lifecycle:lifecycle-extensions:${Versions.LIFECYCLE}"
    const val LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${Versions.FRAGMENT_KTX}"
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}"

    const val HILT = "com.google.dagger:hilt-android:${Versions.HILT}"
    const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.HILT}"

    const val TIMBER = "com.jakewharton.timber:timber:${Versions.TIMBER}"
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT_CONVERTER = "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}"
}