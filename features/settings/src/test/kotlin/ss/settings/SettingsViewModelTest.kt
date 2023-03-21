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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.settings

import app.cash.turbine.test
import app.ss.auth.AuthRepository
import app.ss.lessons.data.repository.user.UserDataRepository
import com.cryart.sabbathschool.test.coroutines.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockAuthRepository: AuthRepository = mockk()
    private val mockUserDataRepository: UserDataRepository = mockk()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        coEvery { mockUserDataRepository.clear() }.returns(Unit)
        coEvery { mockAuthRepository.deleteAccount() }.returns(Unit)

        viewModel = SettingsViewModel(
            authRepository = mockAuthRepository,
            userDataRepository = mockUserDataRepository
        )
    }

    @Test
    fun initialState() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.viewStateFlow.collect() }

        viewModel.viewStateFlow.test {
            awaitItem() shouldBeEqualTo SettingsState()
        }

        job.cancel()
    }

    @Test
    fun deleteAccount() = runTest {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.viewStateFlow.collect() }

        viewModel.viewStateFlow.test {
            awaitItem() shouldBeEqualTo SettingsState()

            viewModel.deleteAccount()

            awaitItem() shouldBeEqualTo SettingsState(showProgress = true)

            awaitItem() shouldBeEqualTo SettingsState(false)
        }

        job.cancel()
    }

}
