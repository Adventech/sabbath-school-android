/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.navigation

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.settings.SSSettingsActivity
import com.cryart.sabbathschool.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppNavigatorImplTest {

    private val mockFirebaseAuth: FirebaseAuth = mockk()

    private lateinit var controller: ActivityController<AppCompatActivity>
    private lateinit var activity: Activity

    private lateinit var navigator: AppNavigator

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.create().start().resume().get()
        navigator = AppNavigatorImpl(mockFirebaseAuth)
    }

    @After
    fun tearDown() {
        activity.finish()
        controller.destroy()
    }

    @Test
    fun `should navigate to Login destination`() {
        every { mockFirebaseAuth.currentUser }.returns(null)

        navigator.navigate(activity, Destination.LOGIN)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo LoginActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to settings destination`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())

        navigator.navigate(activity, Destination.SETTINGS)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SSSettingsActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to login when not authenticated`() {
        every { mockFirebaseAuth.currentUser }.returns(null)

        navigator.navigate(activity, Destination.SETTINGS)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo LoginActivity::class.qualifiedName
    }

    @Test
    fun `should navigate to settings destination via deep-link`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())

        val uri = Uri.Builder()
            .scheme("ss_app")
            .authority("settings")
            .build()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        val clazz = intent.component?.className
        clazz shouldBeEqualTo SSSettingsActivity::class.qualifiedName
    }

    @Test
    fun `should ignore invalid deep-link`() {
        every { mockFirebaseAuth.currentUser }.returns(mockk())

        val uri = Uri.parse("https://stackoverflow.com/")
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldBeNull()
    }
}
