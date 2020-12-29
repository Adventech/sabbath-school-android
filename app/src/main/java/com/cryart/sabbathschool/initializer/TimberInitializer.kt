package com.cryart.sabbathschool.initializer

import android.content.Context
import androidx.startup.Initializer
import com.cryart.sabbathschool.BuildConfig
import com.cryart.sabbathschool.extensions.CrashlyticsTree
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
