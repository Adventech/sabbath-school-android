/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cryart.sabbathschool

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.cryart.sabbathschool.data.di.DaggerSSAppComponent
import com.cryart.sabbathschool.extensions.CrashlyticsTree
import com.google.firebase.database.FirebaseDatabase
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

class SSApplication : DaggerApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        instance = this

        JodaTimeAndroid.init(this)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val displayImageOptions = DisplayImageOptions.Builder()
                .displayer(RoundedBitmapDisplayer(20))
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build()
        val config = ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(displayImageOptions)
                .build()
        ImageLoader.getInstance().init(config)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerSSAppComponent.factory().create(this)
    }

    companion object {
        private lateinit var instance: SSApplication

        @JvmStatic
        operator fun get(context: Context): SSApplication {
            return context.applicationContext as SSApplication
        }

        @JvmStatic
        fun get(): SSApplication {
            return instance
        }
    }
}