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

package com.cryart.sabbathschool.readings.days.components;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
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
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.cryart.sabbathschool.core.extensions.context.ContextHelper;
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.reader.SSWebView;
import com.cryart.sabbathschool.readings.R;

import java.nio.charset.StandardCharsets;
import java.util.List;

import app.ss.models.SSComment;
import app.ss.models.SSReadComments;
import app.ss.models.SSReadHighlights;
import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;

public class SSReadingView extends SSWebView {
    public static final String SEARCH_PROVIDER = "https://www.google.com/search?q=%s";
    public static final String CLIPBOARD_LABEL = "ss_clipboard_label";
    private static final String bridgeName = "SSBridge";

    private GestureDetectorCompat gestureDetector;

    @Nullable
    private ContextMenuCallback contextMenuCallback;
    @Nullable
    private HighlightsCommentsCallback highlightsCommentsCallback;

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
            gestureDetector = new GestureDetectorCompat(context, new GestureListener());
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
        if (contextMenuCallback != null) {
            contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY);
        }
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

    public void setContextMenuCallback(@NonNull ContextMenuCallback contextMenuCallback) {
        this.contextMenuCallback = contextMenuCallback;
    }

    public void setHighlightsCommentsCallback(@NonNull HighlightsCommentsCallback highlightsCommentsCallback) {
        this.highlightsCommentsCallback = highlightsCommentsCallback;
    }

    public void setReadHighlights(SSReadHighlights ssReadHighlights) {
        this.ssReadHighlights = ssReadHighlights;
    }

    public void setReadComments(SSReadComments ssReadComments) {
        this.ssReadComments = ssReadComments;
    }

    public void updateReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions) {
        ssReadViewBridge.setTheme(ssReadingDisplayOptions.themeDisplay(getContext()));
        ssReadViewBridge.setFont(ssReadingDisplayOptions.getFont());
        ssReadViewBridge.setSize(ssReadingDisplayOptions.getSize());
    }

    public void updateHighlights() {
        if (ssReadHighlights != null) {
            ssReadViewBridge.setHighlights(ssReadHighlights.getHighlights());
        }
    }

    public void updateComments() {
        if (ssReadComments != null) {
            ssReadViewBridge.setComments(ssReadComments.getComments());
        }
    }

    public void selectionFinished() {
        if (contextMenuCallback != null) {
            contextMenuCallback.onSelectionFinished();
        }
        contextMenuShown = false;
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
        void onHighlightsReceived(@NonNull SSReadHighlights highlights);

        void onCommentsReceived(@NonNull SSReadComments comments);

        void onVerseClicked(@NonNull String verse);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            if (contextMenuShown && contextMenuCallback != null) {
                contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            selectionFinished();
            return true;
        }
    }

    private class SSWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            ContextHelper.launchWebUrl(view.getContext(), url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            ContextHelper.launchWebUrl(view.getContext(), request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            updateHighlights();
            updateComments();
        }
    }

    @SuppressLint("DefaultLocale")
    public class SSReadViewBridge {
        private final Context context;

        SSReadViewBridge(@NonNull Context context) {
            this.context = context;
        }

        public void highlightSelection(@NonNull final String color, final int highlightId) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (highlightId > 0) {
                        loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s', %d);}", color, highlightId));
                    } else {
                        loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", color));
                    }
                });
            }
        }

        public void unHighlightSelection(final int highlightId) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (highlightId > 0) {
                        loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.unHighlightSelection(%d);}", highlightId));
                    } else {
                        loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.unHighlightSelection();}");
                    }
                });
            }
        }

        public void setFont(@NonNull final String font) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setFont('%s');}", font)));
            }
        }

        public void setSize(@NonNull final String size) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setSize('%s');}", size)));
            }
        }

        public void setTheme(@NonNull final String theme) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setTheme('%s');}", theme)));
            }
        }

        public void setHighlights(@NonNull final String highlights) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setHighlights('%s');}", highlights)));
            }
        }

        public void setComments(@NonNull List<SSComment> comments) {
            for (SSComment comment : comments) {
                setIndividualComment(comment.getComment(), comment.getElementId());
            }
        }

        public void setIndividualComment(@NonNull final String comment, @NonNull final String elementId) {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setComment('%s', '%s');}",
                        Base64.encodeToString(comment.getBytes(), Base64.NO_WRAP), elementId)));
            }
        }

        public void copy() {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.copy();}"));
            }
        }

        public void paste() {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return;
            }

            final String buffer = (String) clip.getItemAt(0).coerceToText(context);

            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.paste('%s');}",
                        Base64.encodeToString(buffer.getBytes(), Base64.NO_WRAP))));
            }
        }

        public void share() {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() ->
                    loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.share();}"));
            }
        }

        public void search() {
            Activity activity = getActivity(context);
            if (activity != null) {
                activity.runOnUiThread(() -> loadUrl("javascript:if(typeof ssReader !== \"undefined\"){ssReader.search();}"));
            }
        }

        /**
         * Receiving serialized ssReadHighlights from webapp
         *
         * @param serializedHighlights :
         */
        @JavascriptInterface
        public void onReceiveHighlights(@NonNull String serializedHighlights) {
            try {
                ssReadHighlights.setHighlights(serializedHighlights);
                if (highlightsCommentsCallback != null) {
                    highlightsCommentsCallback.onHighlightsReceived(ssReadHighlights);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onVerseClick(@NonNull String verse) {
            try {
                String _verse = new String(Base64.decode(verse, Base64.DEFAULT), StandardCharsets.UTF_8);
                if (highlightsCommentsCallback != null) {
                    highlightsCommentsCallback.onVerseClicked(_verse);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onCommentsClick(@NonNull String comments, @NonNull final String inputId) {
            try {
                String commentReceived = new String(Base64.decode(comments, Base64.DEFAULT), StandardCharsets.UTF_8);

                boolean found = false;
                for (SSComment comment : ssReadComments.getComments()) {
                    if (comment.getElementId().equalsIgnoreCase(inputId)) {
                        comment.setComment(commentReceived);
                        found = true;
                    }
                }
                if (!found) {
                    ssReadComments.getComments().add(new SSComment(inputId, commentReceived));
                }
                if (highlightsCommentsCallback != null) {
                    highlightsCommentsCallback.onCommentsReceived(ssReadComments);
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onHighlightClicked(final int highlightId) {
            try {
                Activity activity = getActivity(context);
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        Timber.d(String.valueOf(highlightId));
                        if (contextMenuCallback != null) {
                            contextMenuCallback.onSelectionStarted(lastTouchX, lastTouchY, highlightId);
                        }
                    });
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onCopy(@NonNull String selection) {
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
        public void onSearch(@NonNull String selection) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(SSReadingView.SEARCH_PROVIDER, selection)));
                context.startActivity(intent);
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        @JavascriptInterface
        public void onShare(@NonNull String selection) {
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

        public Activity getActivity(@Nullable Context context) {
            if (context == null) {
                return null;
            } else if (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                } else {
                    return getActivity(((ContextWrapper) context).getBaseContext());
                }
            }
            return null;
        }
    }
}
