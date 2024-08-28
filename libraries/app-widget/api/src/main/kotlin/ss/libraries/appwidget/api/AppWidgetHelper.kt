package ss.libraries.appwidget.api

interface AppWidgetHelper {
    fun refreshAll()
    suspend fun isAdded(): Boolean
    fun syncQuarterly(index: String)
}
