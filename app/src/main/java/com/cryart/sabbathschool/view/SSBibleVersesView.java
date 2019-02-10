/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.view;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crashlytics.android.Crashlytics;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSBibleVerses;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;

import java.io.File;

import static com.cryart.sabbathschool.view.SSReadingView.readFileFromAssets;
import static com.cryart.sabbathschool.view.SSReadingView.readFileFromFiles;

public class SSBibleVersesView extends WebView {
    private String content_app;

    public SSBibleVersesView(final Context context) {
        super(context);
        if (!isInEditMode()) {
            this.setWebViewClient(new WebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
        }
    }

    public SSBibleVersesView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            this.setWebViewClient(new WebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
        }
    }

    public SSBibleVersesView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            this.getSettings().setJavaScriptEnabled(true);
            this.setWebViewClient(new WebViewClient());
        }
    }

    public void loadVerse(SSBibleVerses bibleVersion, String verse){
        String baseUrl = SSConstants.SS_READER_APP_BASE_URL;

        if (content_app == null) {
            final File indexFile = new File(getContext().getFilesDir() + "/index.html");

            if (indexFile.exists()){
                baseUrl = "file:///" + getContext().getFilesDir() + "/";
                content_app = readFileFromFiles(getContext().getFilesDir() + "/index.html");
            } else {
                content_app = readFileFromAssets(getContext(), SSConstants.SS_READER_APP_ENTRYPOINT);
            }
        }

        String verseContent = "";

        try {
            verseContent = bibleVersion.verses.get(verse);
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
        } catch (Exception e){
            Crashlytics.logException(e);
        }
    }
}
