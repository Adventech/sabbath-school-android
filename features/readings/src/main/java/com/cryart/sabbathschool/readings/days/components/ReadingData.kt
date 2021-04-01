package com.cryart.sabbathschool.readings.days.components

import app.ss.lessons.data.model.SSReadComments
import app.ss.lessons.data.model.SSReadHighlights

sealed class ReadingData {

    data class Content(
        val content: String,
        val highlights: SSReadHighlights = SSReadHighlights(),
        val comments: SSReadComments = SSReadComments("", emptyList())
    ) : ReadingData()

    object Empty : ReadingData()
}
