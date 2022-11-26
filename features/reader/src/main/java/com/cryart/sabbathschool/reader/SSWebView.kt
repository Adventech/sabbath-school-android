package com.cryart.sabbathschool.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import com.cryart.sabbathschool.core.misc.SSConstants
import com.cryart.sabbathschool.core.misc.SSHelper
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions
import com.cryart.sabbathschool.core.model.displayTheme
import timber.log.Timber
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
open class SSWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        if (!isInEditMode) {
            this.webViewClient = WebViewClient()
            this.settings.javaScriptEnabled = true
            this.settings.allowFileAccess = true
        }
    }

    open fun loadContent(contentData: String, ssReadingDisplayOptions: SSReadingDisplayOptions) {
        var baseUrl = SSConstants.SS_READER_APP_BASE_URL

        val indexFile = File(context.filesDir.toString() + "/index.html")
        var content = if (indexFile.exists()) {
            baseUrl = "file:///" + context.filesDir + "/"
            SSHelper.readFileFromFiles(context.filesDir.toString() + "/index.html")
        } else {
            SSHelper.readFileFromAssets(context, SSConstants.SS_READER_APP_ENTRYPOINT)
        }
        try {
            val parsedData = contentData.replace("$", "\\$")
            content = content.replace(Regex.fromLiteral("{{content}}"), parsedData)
            content = content.replace("ss-wrapper-light", "ss-wrapper-${ssReadingDisplayOptions.displayTheme(context)}")
            content = content.replace("ss-wrapper-andada", "ss-wrapper-${ssReadingDisplayOptions.font}")
            content = content.replace("ss-wrapper-medium", "ss-wrapper-${ssReadingDisplayOptions.size}")
            loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
