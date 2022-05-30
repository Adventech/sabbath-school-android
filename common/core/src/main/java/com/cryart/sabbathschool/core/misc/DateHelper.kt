package com.cryart.sabbathschool.core.misc

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

object DateHelper {

    fun parseDate(date: String): DateTime? {
        return try {
            DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                .parseLocalDate(date).toDateTimeAtStartOfDay()
        } catch (ex: Exception) {
            Timber.e(ex)
            null
        }
    }

    fun formatDate(date: String, format: String = SSConstants.SS_DATE_FORMAT_OUTPUT_DAY): String {
        return try {
            DateTimeFormat.forPattern(format)
                .print(
                    DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(date)
                ).replace("Saturday", "Sabbath")
                .replaceFirstChar { it.uppercase() }
        } catch (ex: IllegalArgumentException) {
            Timber.e(ex)
            return ""
        }
    }
}
