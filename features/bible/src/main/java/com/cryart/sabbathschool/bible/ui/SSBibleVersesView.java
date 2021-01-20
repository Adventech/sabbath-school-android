/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.bible.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cryart.sabbathschool.core.misc.SSConstants;
import com.cryart.sabbathschool.core.misc.SSHelper;
import com.cryart.sabbathschool.core.model.SSBibleVerses;
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions;

import java.io.File;

import timber.log.Timber;

public class SSBibleVersesView extends WebView {
    private String content_app;

    public SSBibleVersesView(final Context context) {
        super(context);
        initWebView();
    }

    public SSBibleVersesView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public SSBibleVersesView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        if (!isInEditMode()) {
            this.setWebViewClient(new WebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
            this.getSettings().setAllowFileAccess(true);
        }
    }

    public void loadVerse(SSBibleVerses bibleVersion, String verse) {
        String baseUrl = SSConstants.SS_READER_APP_BASE_URL;

        if (content_app == null) {
            final File indexFile = new File(getContext().getFilesDir() + "/index.html");

            if (indexFile.exists()) {
                baseUrl = "file:///" + getContext().getFilesDir() + "/";
                content_app = SSHelper.readFileFromFiles(getContext().getFilesDir() + "/index.html");
            } else {
                content_app = SSHelper.readFileFromAssets(getContext(), SSConstants.SS_READER_APP_ENTRYPOINT);
            }
        }

        try {
            String verseContent = bibleVersion.verses.get(verse);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SSReadingDisplayOptions ssReadingDisplayOptions = new SSReadingDisplayOptions(
                prefs.getString(SSConstants.SS_SETTINGS_THEME_KEY, SSReadingDisplayOptions.SS_THEME_LIGHT),
                prefs.getString(SSConstants.SS_SETTINGS_SIZE_KEY, SSReadingDisplayOptions.SS_SIZE_MEDIUM),
                prefs.getString(SSConstants.SS_SETTINGS_FONT_KEY, SSReadingDisplayOptions.SS_FONT_LATO)
            );

            String content = content_app.replaceAll("\\{\\{content\\}\\}", verseContent);
            content = content.replace("ss-wrapper-light", "ss-wrapper-" + ssReadingDisplayOptions.theme);
            content = content.replace("ss-wrapper-andada", "ss-wrapper-" + ssReadingDisplayOptions.font);
            content = content.replace("ss-wrapper-medium", "ss-wrapper-" + ssReadingDisplayOptions.size);

            loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
