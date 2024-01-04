/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.navigation

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.core.navigation.toUri
import com.cryart.sabbathschool.lessons.ui.lessons.SSLessonsActivity
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesActivity
import com.cryart.sabbathschool.lessons.ui.readings.SSReadingActivity
import com.cryart.sabbathschool.ui.login.LoginActivity
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.misc.SSConstants
import ss.prefs.api.test.FakeSSPrefs
import ss.settings.SettingsActivity

@RunWith(AndroidJUnit4::class)
class AppNavigatorImplTest {

    private val fakeSSPrefs = FakeSSPrefs()
    private val fakeAuthRepository = FakeAuthRepository()

    private lateinit var controller: ActivityController<AppCompatActivity>
    private lateinit var activity: Activity

    private lateinit var navigator: AppNavigator

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.create().start().resume().get()

        fakeSSPrefs.quarterlyIndexDelegate = { "index" }
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }

        navigator = AppNavigatorImpl(
            ssPrefs = fakeSSPrefs,
            authRepository = fakeAuthRepository,
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @After
    fun tearDown() {
        activity.finish()
        controller.destroy()
    }

    @Test
    fun `should navigate to Login destination`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        navigator.navigate(activity, Destination.LOGIN)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo LoginActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to settings destination`() {
        navigator.navigate(activity, Destination.SETTINGS)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SettingsActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to login when not authenticated`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        navigator.navigate(activity, Destination.SETTINGS)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo LoginActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to settings destination via deep-link`() {
        val uri = Destination.SETTINGS.toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SettingsActivity::class.qualifiedName
    }

    @Test
    fun `should add extras from deep-link into intent extras`() {
        val uri = Destination.READ.toUri("key" to "value")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.getStringExtra("key") shouldBeEqualTo "value"
    }

    @Test
    fun `should ignore invalid deep-link`() {
        val uri = Uri.parse("https://stackoverflow.com/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldBeNull()
    }

    @Test
    fun `should navigate to login when not authenticated - web-link`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        val uri = Uri.parse("https://sabbath-school.adventech.io/en/2021-03")

        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo LoginActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to lessons screen - web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/en/2021-03")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SSLessonsActivity::class.qualifiedName

        intent.getStringExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA) shouldBeEqualTo "en-2021-03"
    }

    @Test
    fun `should navigate to read screen - web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/en/2021-03/03/07-friday-further-thought/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SSReadingActivity::class.qualifiedName

        intent.getStringExtra(SSConstants.SS_LESSON_INDEX_EXTRA) shouldBeEqualTo "en-2021-03-03"
        intent.getStringExtra(SSConstants.SS_READ_POSITION_EXTRA) shouldBeEqualTo "6"
    }

    @Test
    fun `should launch normal flow for invalid web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/03/07-friday-further-thought/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo QuarterliesActivity::class.qualifiedName
    }
}
