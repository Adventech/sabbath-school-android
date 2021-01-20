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

package com.cryart.sabbathschool.lessons.ui.readings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.view.GestureDetectorCompat;
import com.cryart.sabbathschool.core.misc.SSConstants;
import com.cryart.sabbathschool.core.misc.SSHelper;
import com.cryart.sabbathschool.core.model.SSRead;
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.lessons.R;
import com.cryart.sabbathschool.lessons.data.model.SSComment;
import com.cryart.sabbathschool.lessons.data.model.SSReadComments;
import com.cryart.sabbathschool.lessons.data.model.SSReadHighlights;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;

public class SSReadingView extends WebView {
    public static final String SEARCH_PROVIDER = "https://www.google.com/search?q=%s";
    public static final String CLIPBOARD_LABEL = "ss_clipboard_label";
    private static final String bridgeName = "SSBridge";

    private GestureDetectorCompat gestureDetector;
    private ContextMenuCallback contextMenuCallback;
    private HighlightsCommentsCallback highlightsCommentsCallback;
    private SSReadingDisplayOptions ssReadingDisplayOptions;
    private String ssReaderContent;

    public SSReadViewBridge ssReadViewBridge;

    private float lastTouchX;
    private float lastTouchY;
    private boolean textAreaFocused = false;
    public boolean contextMenuShown = false;

    public SSReadHighlights ssReadHighlights;
    public SSReadComments ssReadComments;


    public SSReadingView(final Context context) {
        super(context);
        initWebView(context);
    }

    public SSReadingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initWebView(context);
    }

    public SSReadingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initWebView(context);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView(@NonNull Context context) {
        if (!isInEditMode()) {
            gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
            ssReadViewBridge = new SSReadViewBridge(context);
            this.setWebViewClient(new SSWebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
            this.getSettings().setAllowFileAccess(true);
            this.addJavascriptInterface(ssReadViewBridge, bridgeName);
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        if (textAreaFocused) {
            return super.startActionMode(callback, type);
        }
        return startActionMode(callback);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY);
        contextMenuShown = true;
        return this.emptyActionMode();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchY = event.getY();
            lastTouchX = event.getX();
        }
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void setContextMenuCallback(ContextMenuCallback contextMenuCallback) {
        this.contextMenuCallback = contextMenuCallback;
    }

    public void setHighlightsCommentsCallback(HighlightsCommentsCallback highlightsCommentsCallback) {
        this.highlightsCommentsCallback = highlightsCommentsCallback;
    }

    public void setReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions) {
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;
    }

    public void setReadHighlights(SSReadHighlights ssReadHighlights) {
        this.ssReadHighlights = ssReadHighlights;
    }

    public void setReadComments(SSReadComments ssReadComments) {
        this.ssReadComments = ssReadComments;
    }

    public void updateReadingDisplayOptions() {
        if (this.ssReadingDisplayOptions != null) {
            ssReadViewBridge.setTheme(ssReadingDisplayOptions.theme);
            ssReadViewBridge.setFont(ssReadingDisplayOptions.font);
            ssReadViewBridge.setSize(ssReadingDisplayOptions.size);
        }
    }

    public void updateHighlights() {
        if (ssReadHighlights != null) {
            ssReadViewBridge.setHighlights(ssReadHighlights.highlights);
        }
    }

    public void updateComments() {
        if (ssReadComments != null) {
            ssReadViewBridge.setComments(ssReadComments.comments);
        }
    }

    public void selectionFinished() {
        contextMenuCallback.onSelectionFinished();
        contextMenuShown = false;
    }

    public void loadRead(SSRead ssRead) {
        // We don't want any flickering of themes, right?
        String baseUrl = SSConstants.SS_READER_APP_BASE_URL;

        if (ssReaderContent == null) {
            final File indexFile = new File(getContext().getFilesDir() + "/index.html");

            if (indexFile.exists()) {
                baseUrl = "file:///" + getContext().getFilesDir() + "/";
                ssReaderContent = SSHelper.readFileFromFiles(getContext().getFilesDir() + "/index.html");
            } else {
                ssReaderContent = SSHelper.readFileFromAssets(getContext(), SSConstants.SS_READER_APP_ENTRYPOINT);
            }
        }

        String content = ssReaderContent.replace("{{content}}", ssRead.content);

        content = content.replace("ss-wrapper-light", "ss-wrapper-" + ssReadingDisplayOptions.theme);
        content = content.replace("ss-wrapper-andada", "ss-wrapper-" + ssReadingDisplayOptions.font);
        content = content.replace("ss-wrapper-medium", "ss-wrapper-" + ssReadingDisplayOptions.size);

        loadDataWithBaseURL(baseUrl, content, "text/html", "utf-8", null);
    }

    public ActionMode emptyActionMode() {
        return new ActionMode() {
            @Override
            public void setTitle(CharSequence title) {
            }

            @Override
            public void setTitle(int resId) {
            }

            @Override
            public void setSubtitle(CharSequence subtitle) {
            }

            @Override
            public void setSubtitle(int resId) {
            }

            @Override
            public void setCustomView(View view) {
            }

            @Override
            public void invalidate() {
            }

            @Override
            public void finish() {
            }

            @Override
            public Menu getMenu() {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public CharSequence getSubtitle() {
                return null;
            }

            @Override
            public View getCustomView() {
                return null;
            }

            @Override
            public MenuInflater getMenuInflater() {
                return null;
            }
        };
    }

    public interface ContextMenuCallback {
        void onSelectionStarted(float x, float y);

        void onSelectionStarted(float x, float y, int highlightId);

        void onSelectionFinished();

    }

    public interface HighlightsCommentsCallback {
        void onHighlightsReceived(SSReadHighlights ssReadHighlights);

        void onCommentsReceived(SSReadComments ssReadComments);

        void onVerseClicked(String verse);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            if (contextMenuShown) contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            selectionFinished();
            return true;
        }
    }

    private class SSWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            openExternalLink(view.getContext(), url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            openExternalLink(view.getContext(), request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            updateReadingDisplayOptions();
            updateHighlights();
            updateComments();
        }

        private void openExternalLink(Context context, String url) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setStartAnimations(context, R.anim.slide_up, android.R.anim.fade_out)
                .setExitAnimations(context, android.R.anim.fade_in, R.anim.slide_down);
            try {
                CustomTabsIntent intent = builder.build();
                intent.launchUrl(context, Uri.parse(url));
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("unused")
    public class SSReadViewBridge {
        Context context;

        SSReadViewBridge(Context c) {
            context = c;
        }

        public void highlightSelection(final String color, final int highlightId) {
            ((SSReadingActivity) context).runOnUiThread(() -> {
                if (highlightId > 0) {
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s', %d);}", color, highlightId));
                } else {
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", color));
                }
            });
        }

        public void unHighlightSelection(final int highlightId) {
            ((SSReadingActivity) context).runOnUiThread(() -> {
                if (highlightId > 0) {
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.unHighlightSelection(%d);}", highlightId));
                } else {
                    loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.unHighlightSelection();}");
                }
            });
        }

        public void setFont(final String font) {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setFont('%s');}", font)));
        }

        public void setSize(final String size) {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setSize('%s');}", size)));
        }

        public void setTheme(final String theme) {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setTheme('%s');}", theme)));
        }

        public void setHighlights(final String highlights) {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setHighlights('%s');}", highlights)));
        }

        public void setComments(List<SSComment> comments) {
            for (SSComment comment : comments) {
                setIndividualComment(comment.comment, comment.elementId);
            }
        }

        public void setIndividualComment(final String comment, final String elementId) {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setComment('%s', '%s');}", Base64.encodeToString(comment.getBytes(), Base64.NO_WRAP), elementId)));
        }

        public void copy() {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.copy();}"));
        }

        public void paste() {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return;
            }

            final String buffer = (String) clip.getItemAt(0).coerceToText(context);

            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.paste('%s');}", Base64.encodeToString(buffer.getBytes(), Base64.NO_WRAP))));
        }

        public void share() {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.share();}"));
        }

        public void search() {
            ((SSReadingActivity) context).runOnUiThread(() ->
                loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.search();}"));
        }

        /**
         * Receiving serizlied ssReadHighlights from webapp
         *
         * @param serializedHighlights
         */
        @JavascriptInterface
        public void onReceiveHighlights(String serializedHighlights) {
            try {
                ssReadHighlights.highlights = serializedHighlights;
                highlightsCommentsCallback.onHighlightsReceived(ssReadHighlights);
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onVerseClick(String verse) {
            try {
                String _verse = new String(Base64.decode(verse, Base64.DEFAULT), StandardCharsets.UTF_8);
                highlightsCommentsCallback.onVerseClicked(_verse);
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onCommentsClick(String comments, final String inputId) {

            try {
                String commentReceived = new String(Base64.decode(comments, Base64.DEFAULT), StandardCharsets.UTF_8);

                boolean found = false;
                for (SSComment comment : ssReadComments.comments) {
                    if (comment.elementId.equalsIgnoreCase(inputId)) {
                        comment.comment = commentReceived;
                        found = true;
                    }
                }
                if (!found) {
                    ssReadComments.comments.add(new SSComment(inputId, commentReceived));
                }
                highlightsCommentsCallback.onCommentsReceived(ssReadComments);

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onHighlightClicked(final int highlightId) {
            try {
                ((SSReadingActivity) context).runOnUiThread(() -> {
                    Timber.d(String.valueOf(highlightId));
                    contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY, highlightId);
                });

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onCopy(String selection) {
            try {
                ClipboardManager _clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(SSReadingView.CLIPBOARD_LABEL, selection);
                _clipboard.setPrimaryClip(clip);
                Toast.makeText(context, context.getString(R.string.ss_reading_copied), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onSearch(String selection) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(SSReadingView.SEARCH_PROVIDER, selection)));
                context.startActivity(intent);
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onShare(String selection) {
            try {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, selection);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.ss_reading_share_to)));
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void focusin() {
            textAreaFocused = true;
        }

        @JavascriptInterface
        public void focusout() {
            textAreaFocused = false;
        }
    }
}
