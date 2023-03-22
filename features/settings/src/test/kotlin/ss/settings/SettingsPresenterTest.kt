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

import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import ss.settings.SettingsScreen.Event
import ss.settings.repository.SettingsRepository

class SettingsPresenterTest {

    private val navigator = FakeNavigator()

    @Test
    fun `present - emit empty entities then default entities state`() = runTest {
        val entities = listOf(DividerEntity("1"), DividerEntity("2"))

        val presenter = SettingsPresenter(
            repository = TestRepository(entities),
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
            repository = TestRepository(entities),
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
}

class TestRepository(
    private val entities: List<ListEntity>
) : SettingsRepository {
    override fun buildEntities(
        onCheckedChange: (Boolean) -> Unit,
        onGoToUrl: (String) -> Unit
    ): List<ListEntity> = entities
}
