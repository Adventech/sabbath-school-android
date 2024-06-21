/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package ss.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.libraries.circuit.navigation.LoginScreen
import ss.libraries.circuit.navigation.SettingsScreen
import ss.settings.repository.SettingsEntity
import ss.settings.repository.SettingsRepository

@RunWith(AndroidJUnit4::class)
class SettingsPresenterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val navigator = FakeNavigator(SettingsScreen)

    @Test
    fun `present - emit empty entities then default entities state`() = runTest {
        val entities = listOf(DividerEntity("1"), DividerEntity("2"))

        val presenter = SettingsPresenter(
            context = context,
            repository = FakeRepository(entities),
            navigator = navigator
        )

        presenter.test {
            awaitItem().entities shouldBeEqualTo emptyList()
            awaitItem().entities shouldBeEqualTo entities
        }
    }

    @Test
    fun `present - navigator pop on NavBack event`() = runTest {
        val entities = listOf(DividerEntity("1"), DividerEntity("2"))

        val presenter = SettingsPresenter(
            context = context,
            repository = FakeRepository(entities),
            navigator = navigator
        )

        presenter.test {
            awaitItem().entities shouldBeEqualTo emptyList()
            val state = awaitItem()
            state.entities shouldBeEqualTo entities

            state.eventSick(Event.NavBack)

            navigator.awaitPop()
        }
    }

    @Test
    fun `present - Event account delete confirmed`() = runTest {
        val entities = listOf(DividerEntity("1"), DividerEntity("2"))

        val presenter = SettingsPresenter(
            context = context,
            repository = FakeRepository(entities),
            navigator = navigator
        )

        presenter.test {
            awaitItem().entities shouldBeEqualTo emptyList()
            val state = awaitItem()
            state.entities shouldBeEqualTo entities

            state.eventSick(Event.AccountDeleteConfirmed)

            navigator.awaitResetRoot().newRoot shouldBeEqualTo LoginScreen
        }
    }
}

internal class FakeRepository(
    private val entities: List<ListEntity>
) : SettingsRepository {
    override fun entitiesFlow(onEntityClick: (SettingsEntity) -> Unit): Flow<List<ListEntity>> {
        return flowOf(entities)
    }

    override fun setReminderTime(hour: Int, minute: Int) = Unit

    override fun signOut() = Unit

    override fun deleteAccount() = Unit

    override fun removeAllDownloads() = Unit
}
