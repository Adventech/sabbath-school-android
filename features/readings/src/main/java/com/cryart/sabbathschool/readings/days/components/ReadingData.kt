package com.cryart.sabbathschool.readings.days.components

sealed class ReadingData {

    data class Content(
        val content: String,
    ) : ReadingData()

    object Empty : ReadingData()
}
