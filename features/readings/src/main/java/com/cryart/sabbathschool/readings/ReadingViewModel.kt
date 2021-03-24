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

package com.cryart.sabbathschool.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryart.sabbathschool.readings.components.model.AppBarData
import com.cryart.sabbathschool.readings.components.model.ReadingDay
import com.cryart.sabbathschool.readings.components.model.ReadingDaysData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor() : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<ReadUiState>(ReadUiState.Loading)
    val uiStateFlow: Flow<ReadUiState> get() = _uiStateFlow

    private val _appBarDataFlow = MutableStateFlow<AppBarData?>(null)
    val appBarDataFlow: Flow<AppBarData> get() = _appBarDataFlow.mapNotNull { it }

    private val _readDaysFlow = MutableStateFlow<ReadingDaysData?>(null)
    val readDaysFlow: Flow<ReadingDaysData> get() = _readDaysFlow.mapNotNull { it }

    fun onPageSelected(position: Int) {
    }

    fun loadData() = viewModelScope.launch {
        delay(2000)
        _uiStateFlow.emit(ReadUiState.Success)

        val data = AppBarData(
            "https://sabbath-school-stage.adventech.io/api/v1/images/global/2021-01/13/cover.png",
            "Divine \"Magnet\"", date = "MONDAY. 22 March"
        )

        val days = mutableListOf<ReadingDay>()
        for (i in 1..7) {
            days.add(ReadingDay("$i", "12", "path"))
        }
        _readDaysFlow.emit(ReadingDaysData(days))

        for (i in 1..10) {
            delay(3000)
            _appBarDataFlow.emit(data.copy(date = "Monday. ${20 + i} March"))
        }
    }
}
