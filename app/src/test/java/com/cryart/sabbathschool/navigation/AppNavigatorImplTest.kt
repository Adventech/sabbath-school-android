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
import androidx.core.os.BundleCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.slack.circuit.runtime.screen.Screen
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
import ss.libraries.circuit.navigation.DocumentScreen
import ss.libraries.circuit.navigation.HomeNavScreen
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.ResourceScreen

private const val ARG_EXTRA_SCREEN = "extra_screen"

@RunWith(AndroidJUnit4::class)
class AppNavigatorImplTest {

    private val fakeAuthRepository = FakeAuthRepository()

    private lateinit var controller: ActivityController<AppCompatActivity>
    private lateinit var activity: Activity

    private lateinit var navigator: AppNavigator

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.create().start().resume().get()

        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }

        navigator = AppNavigatorImpl(
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
    fun `should navigate to login when not authenticated`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        navigator.navigate(activity, Destination.ABOUT)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo LoginScreen
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

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo LoginScreen
    }

    @Test
    fun `should navigate to resource screen - web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/en/2021-03")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo ResourceScreen("en/ss/2021-03")
    }

    @Test
    fun `should navigate to resource screen - aij - web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/resources/en/aij/2025-00-bb-pb")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo ResourceScreen("en/aij/2025-00-bb-pb")
    }

    @Test
    fun `should navigate to document screen - web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/en/2021-03/03/07-friday-further-thought/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo DocumentScreen("en/ss/2021-03/03", null, null)
    }

    @Test
    fun `should launch normal flow for invalid web-link`() {
        val uri = Uri.parse("https://sabbath-school.adventech.io/03/07-friday-further-thought/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val screen = BundleCompat.getParcelable(intent.extras!!, ARG_EXTRA_SCREEN, Screen::class.java)
        screen shouldBeEqualTo HomeNavScreen
    }
}
