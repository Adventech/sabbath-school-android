package com.cryart.sabbathschool.readings.components

import android.app.Activity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.databinding.ComponentReadingAppBarBinding
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppBarComponentTest {

    private val mockLifecycleOwner: LifecycleOwner = mockk()

    private lateinit var activity: Activity

    private lateinit var binding: ComponentReadingAppBarBinding
    private lateinit var component: AppBarComponent

    @Before
    fun setup() {
        val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.create().start().resume().get()

        binding = ComponentReadingAppBarBinding.inflate(
            activity.layoutInflater,
            LinearLayout(activity)
        )

        component = AppBarComponent(mockLifecycleOwner, binding)
    }

    @Test
    fun `should hide appBar when visibilityFlow emits false`() {
        val lifecycle = LifecycleRegistry(mockLifecycleOwner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        every { mockLifecycleOwner.lifecycle }.returns(lifecycle)

        component.collect(flowOf(false), emptyFlow())

        binding.appBar.isVisible.shouldBeFalse()
    }

    @Test
    fun `should show appBar when visibilityFlow emits true`() {
        val lifecycle = LifecycleRegistry(mockLifecycleOwner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        every { mockLifecycleOwner.lifecycle }.returns(lifecycle)

        component.collect(flowOf(true), emptyFlow())

        binding.appBar.isVisible.shouldBeTrue()
    }

    @Test
    fun `should not collect any from visibilityFlow when Lifecycle is not ON_START`() {
        val lifecycle = LifecycleRegistry(mockLifecycleOwner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        every { mockLifecycleOwner.lifecycle }.returns(lifecycle)

        binding.appBar.isVisible = false

        component.collect(flowOf(true), emptyFlow())

        binding.appBar.isVisible.shouldBeFalse()
    }

    @Test
    fun `should set title and subTitle from dataFlow when data is AppBarData Title`() {
        val lifecycle = LifecycleRegistry(mockLifecycleOwner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        every { mockLifecycleOwner.lifecycle }.returns(lifecycle)

        binding.collapsingToolbar.title = ""
        binding.subtitle.text = ""

        component.collect(emptyFlow(), flowOf(AppBarData.Title("Title", "Sub-Title")))

        binding.collapsingToolbar.title shouldBeEqualTo "Title"
        binding.subtitle.text shouldBeEqualTo "Sub-Title"
    }

    @Test
    fun `should not collect any from dataFlow when Lifecycle is not ON_START`() {
        val lifecycle = LifecycleRegistry(mockLifecycleOwner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        every { mockLifecycleOwner.lifecycle }.returns(lifecycle)

        binding.collapsingToolbar.title = ""
        binding.subtitle.text = ""

        component.collect(emptyFlow(), flowOf(AppBarData.Title("Title", "Sub-Title")))

        binding.collapsingToolbar.title shouldBeEqualTo ""
        binding.subtitle.text shouldBeEqualTo ""
    }
}
