package ss.lessons.api.helper

import app.ss.models.SSQuarterlyInfo

/** Common implementations for content sync. */
interface SyncHelper {
    suspend fun syncQuarterlyInfo(index: String): SSQuarterlyInfo?
    fun syncQuarterlies(language: String)
    fun syncPublishingInfo(country: String, language: String)
}
