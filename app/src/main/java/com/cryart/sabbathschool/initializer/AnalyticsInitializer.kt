package com.cryart.sabbathschool.initializer

import android.content.Context
import androidx.startup.Initializer
import com.cryart.sabbathschool.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class AnalyticsInitializer(
    private val analytics: FirebaseAnalytics = Firebase.analytics
) : Initializer<Unit> {

    override fun create(context: Context) {
        analytics
            .setUserProperty("version_code", BuildConfig.VERSION_CODE.toString())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
