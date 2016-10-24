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
import android.util.Log;
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
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;

public class SSReadingView extends WebView {
    public static final String SEARCH_PROVIDER = "https://www.google.com/search?q=%s";
    public static final String CLIPBOARD_LABEL = "ss_clipboard_label";

    private static final String TAG = SSReadingView.class.getSimpleName();
    private static final String bridgeName = "SSBridge";


    private GestureDetectorCompat gestureDetector;
    private ContextMenuCallback contextMenuCallback;
    private SSReadingDisplayOptions ssReadingDisplayOptions;

    public SSReadViewBridge ssReadViewBridge;

    private float LastTouchX;
    private float LastTouchY;
    public boolean contextMenuShown = false;

    public String highlights;


    public SSReadingView(final Context context) {
        super(context);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        ssReadViewBridge = new SSReadViewBridge(context);
        this.setWebViewClient(new SSWebViewClient());
        this.addJavascriptInterface(ssReadViewBridge, bridgeName);

    }

    public SSReadingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        ssReadViewBridge = new SSReadViewBridge(context);
        this.setWebViewClient(new SSWebViewClient());
        this.addJavascriptInterface(ssReadViewBridge, bridgeName);
    }

    public SSReadingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        ssReadViewBridge = new SSReadViewBridge(context);
        this.setWebViewClient(new SSWebViewClient());
        this.addJavascriptInterface(ssReadViewBridge, bridgeName);
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

    public void setReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions){
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;
    }

    public void updateReadingDisplayOptions(){
        if (this.ssReadingDisplayOptions != null) {
            ssReadViewBridge.setTheme(ssReadingDisplayOptions.theme);
            ssReadViewBridge.setFont(ssReadingDisplayOptions.font);
            ssReadViewBridge.setSize(ssReadingDisplayOptions.size);
        }
    }

    public void selectionFinished(){
        contextMenuCallback.onSelectionFinished();
        contextMenuShown = false;
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
            updateReadingDisplayOptions();
            super.onPageFinished(view, url);
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
                    loadUrl(String.format("javascript:ssReader.highlightSelection('%s');", color));
                }
            });
        }

        public void unHighlightSelection(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:ssReader.unHighlightSelection();");
                }
            });
        }

        public void setFont(final String font){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:ssReader.setFont('%s');", font));
                }
            });
        }

        public void setSize(final String size){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:ssReader.setSize('%s');", size));
                }
            });
        }

        public void setTheme(final String theme){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:ssReader.setTheme('%s');", theme));
                }
            });
        }

        public void setHighlights(final String highlights){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, highlights);
                    loadUrl(String.format("javascript:ssReader.setHighlights('%s');", highlights));
                }
            });
        }

        public void setIndividualComment(final String comment, final String elementId){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl(String.format("javascript:ssReader.setComment('%s', '%s');", comment, elementId));
                }
            });
        }

        public void copy(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:ssReader.copy()");
                }
            });
        }

        public void share(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:ssReader.share()");
                }
            });
        }

        public void search(){
            ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadUrl("javascript:ssReader.search()");
                }
            });
        }

        /**
         * Receiving serizlied highlights from webapp
         * @param serializedHighlights
         */
        @JavascriptInterface
        public void onReceiveHighlights(String serializedHighlights){
            try {

            } catch (Exception e){}
        }


        @JavascriptInterface
        public void onCommentsClick(String comments, final String inputId){
            try {
                comments = new String(Base64.decode(comments, Base64.DEFAULT), "UTF-8");

                new MaterialDialog.Builder(context)
                        .title("Comment")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Enter your comment", comments, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                setIndividualComment(Base64.encodeToString(input.toString().getBytes(), Base64.DEFAULT), inputId);
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
                Toast.makeText(context.getApplicationContext(), "Selection copied!", Toast.LENGTH_LONG).show();
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
                context.startActivity(Intent.createChooser(sendIntent, "Share Sabbath School to:"));
            } catch (Exception e){}
        }
    }
}
