package ss.libraries.appwidget.test

import ss.libraries.appwidget.api.AppWidgetHelper

/** Fake implementation of [AppWidgetHelper] for use in tests. */
class FakeAppWidgetHelper : AppWidgetHelper {

    var widgetsRefreshed: Boolean = false
        private set
    var syncedIndex: String? = null
        private set

    override fun refreshAll() {
        widgetsRefreshed = true
    }

    override suspend fun isAdded(): Boolean {
        return true
    }

    override fun syncQuarterly(index: String) {
        syncedIndex = index
    }
}
