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


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.GestureDetectorCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.model.SSComment;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.model.SSReadComments;
import com.cryart.sabbathschool.model.SSReadHighlights;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class SSReadingView extends WebView {
    public static final String SEARCH_PROVIDER = "https://www.google.com/search?q=%s";
    public static final String CLIPBOARD_LABEL = "ss_clipboard_label";

    private static final String TAG = SSReadingView.class.getSimpleName();
    private static final String bridgeName = "SSBridge";

    private GestureDetectorCompat gestureDetector;
    private ContextMenuCallback contextMenuCallback;
    private HighlightsCommentsCallback highlightsCommentsCallback;
    private SSReadingDisplayOptions ssReadingDisplayOptions;
    private String ssReaderContent;

    public SSReadViewBridge ssReadViewBridge;

    private float LastTouchX;
    private float LastTouchY;
    public boolean contextMenuShown = false;

    public SSReadHighlights ssReadHighlights;
    public SSReadComments ssReadComments;


    public SSReadingView(final Context context) {
        super(context);
        if (!isInEditMode()) {
            gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
            ssReadViewBridge = new SSReadViewBridge(context);
            this.setWebViewClient(new SSWebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(ssReadViewBridge, bridgeName);
        }
    }

    public SSReadingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
            ssReadViewBridge = new SSReadViewBridge(context);
            this.setWebViewClient(new SSWebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(ssReadViewBridge, bridgeName);
        }
    }

    public SSReadingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
            ssReadViewBridge = new SSReadViewBridge(context);
            this.setWebViewClient(new SSWebViewClient());
            this.getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(ssReadViewBridge, bridgeName);
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return startActionMode(callback);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        contextMenuCallback.onSelectionStarted(LastTouchX, LastTouchY);
        contextMenuShown = true;
        return this.emptyActionMode();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            LastTouchY = event.getY();
            LastTouchX = event.getX();
        }
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void setContextMenuCallback(ContextMenuCallback contextMenuCallback){
        this.contextMenuCallback = contextMenuCallback;
    }

    public void setHighlightsCommentsCallback(HighlightsCommentsCallback highlightsCommentsCallback){
        this.highlightsCommentsCallback = highlightsCommentsCallback;
    }

    public void setReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions){
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;
    }

    public void setReadHighlights(SSReadHighlights ssReadHighlights){
        this.ssReadHighlights = ssReadHighlights;
    }

    public void setReadComments(SSReadComments ssReadComments){
        this.ssReadComments = ssReadComments;
    }

    public void updateReadingDisplayOptions(){
        if (this.ssReadingDisplayOptions != null) {
            ssReadViewBridge.setTheme(ssReadingDisplayOptions.theme);
            ssReadViewBridge.setFont(ssReadingDisplayOptions.font);
            ssReadViewBridge.setSize(ssReadingDisplayOptions.size);
        }
    }

    public void updateHighlights(){
        ssReadViewBridge.setHighlights(ssReadHighlights.highlights);
    }

    public void updateComments(){
        ssReadViewBridge.setComments(ssReadComments.comments);
    }

    public void selectionFinished(){
        contextMenuCallback.onSelectionFinished();
        contextMenuShown = false;
    }

    public static String readFileFromAssets(Context context, String assetPath){
        StringBuilder buf = new StringBuilder();
        try {
            InputStream json = context.getAssets().open(assetPath);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            return buf.toString();

        } catch (IOException e){ return "";  }
    }

    public void loadRead(SSRead ssRead){
        // We don't want any flickering of themes, right?
        if (ssReaderContent == null){
            ssReaderContent = readFileFromAssets(getContext(), "reader/index.html");
        }

        String content = ssReaderContent.replaceAll("\\{\\{content\\}\\}", ssRead.content);

        content = content.replace("ss-wrapper-light", "ss-wrapper-" + ssReadingDisplayOptions.theme);
        content = content.replace("ss-wrapper-andada", "ss-wrapper-" + ssReadingDisplayOptions.font);
        content = content.replace("ss-wrapper-medium", "ss-wrapper-" + ssReadingDisplayOptions.size);

        loadDataWithBaseURL("file:///android_asset/reader/", content, "text/html", "utf-8", null);
    }

    public ActionMode emptyActionMode() {
        return new ActionMode() {
            @Override public void setTitle(CharSequence title) {}
            @Override public void setTitle(int resId) {}
            @Override public void setSubtitle(CharSequence subtitle) {}
            @Override public void setSubtitle(int resId) {}
            @Override public void setCustomView(View view) {}
            @Override public void invalidate() {}
            @Override public void finish() {}
            @Override public Menu getMenu() { return null; }
            @Override public CharSequence getTitle() { return null; }
            @Override public CharSequence getSubtitle() { return null; }
            @Override public View getCustomView() { return null; }
            @Override public MenuInflater getMenuInflater() { return null; }
        };
    }

    public interface ContextMenuCallback {
        public void onSelectionStarted(float x, float y);
        public void onSelectionFinished();

    }

    public interface HighlightsCommentsCallback {
        public void onHighlightsReceived(SSReadHighlights ssReadHighlights);
        public void onCommentsReceived(SSReadComments ssReadComments);
        public void onVerseClicked(String verse);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            if (contextMenuShown) contextMenuCallback.onSelectionStarted(LastTouchX, LastTouchY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            selectionFinished();
            return true;
        }
    }

    private class SSWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            updateReadingDisplayOptions();
            updateHighlights();
            updateComments();
        }
    }

    public class SSReadViewBridge {
        Context context;
        SSReadViewBridge(Context c) {
            context = c;
        }

        public void highlightSelection(final String color){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.highlightSelection('%s');}", color));
                }
            });
        }

        public void unHighlightSelection(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:if(ssReader){ssReader.unHighlightSelection();}");
                }
            });
        }

        public void setFont(final String font){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.setFont('%s');}", font));
                }
            });
        }

        public void setSize(final String size){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.setSize('%s');}", size));
                }
            });
        }

        public void setTheme(final String theme){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.setTheme('%s');}", theme));
                }
            });
        }

        public void setHighlights(final String highlights){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.setHighlights('%s');}", highlights));
                }
            });
        }

        public void setComments(List<SSComment> comments){
            for(SSComment comment : comments){
                setIndividualComment(comment.comment, comment.elementId);
            }
        }

        public void setIndividualComment(final String comment, final String elementId){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:if(ssReader){ssReader.setComment('%s', '%s');}", Base64.encodeToString(comment.getBytes(), Base64.NO_WRAP), elementId));
                }
            });
        }

        public void copy(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:if(ssReader){ssReader.copy();}");
                }
            });
        }

        public void share(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:if(ssReader){ssReader.share();}");
                }
            });
        }

        public void search(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:if(ssReader){ssReader.search();}");
                }
            });
        }

        /**
         * Receiving serizlied ssReadHighlights from webapp
         * @param serializedHighlights
         */
        @JavascriptInterface
        public void onReceiveHighlights(String serializedHighlights){
            try {
                ssReadHighlights.highlights = serializedHighlights;
                highlightsCommentsCallback.onHighlightsReceived(ssReadHighlights);
            } catch (Exception e){}
        }

        @JavascriptInterface
        public void onVerseClick(String verse){
            try {
                String _verse = new String(Base64.decode(verse, Base64.DEFAULT), "UTF-8");
                highlightsCommentsCallback.onVerseClicked(_verse);
            } catch (Exception e){}
        }

        @JavascriptInterface
        public void onCommentsClick(String comments, final String inputId){
            try {
                comments = new String(Base64.decode(comments, Base64.DEFAULT), "UTF-8");

                new MaterialDialog.Builder(context)
                        .title(context.getString(R.string.ss_reading_comment))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(context.getString(R.string.ss_reading_enter_comment), comments, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                boolean found = false;
                                for(SSComment comment : ssReadComments.comments){
                                    if (comment.elementId.equalsIgnoreCase(inputId)){
                                        comment.comment = input.toString();
                                        found = true;
                                    }
                                }
                                if (!found){
                                    ssReadComments.comments.add(new SSComment(inputId, input.toString()));
                                }
                                highlightsCommentsCallback.onCommentsReceived(ssReadComments);
                                setIndividualComment(input.toString(), inputId);
                            }
                        }).show();

            } catch (Exception e){}
        }

        @JavascriptInterface
        public void onCopy(String selection){
            try {
                ClipboardManager _clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(SSReadingView.CLIPBOARD_LABEL, selection);
                _clipboard.setPrimaryClip(clip);
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.ss_reading_copied), Toast.LENGTH_LONG).show();
            } catch (Exception e){}
        }

        @JavascriptInterface
        public void onSearch(String selection){
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(SSReadingView.SEARCH_PROVIDER, selection)));
                context.startActivity(intent);
            } catch (Exception e){}
        }

        @JavascriptInterface
        public void onShare(String selection){
            try {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, selection);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.ss_reading_share_to)));
            } catch (Exception e){}
        }
    }
}
