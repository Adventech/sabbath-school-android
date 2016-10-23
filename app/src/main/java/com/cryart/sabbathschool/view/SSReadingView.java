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

import com.afollestad.materialdialogs.MaterialDialog;

public class SSReadingView extends WebView {
    private final String bridgeName = "SSBridge";
    private float LastTouchX;
    private float LastTouchY;
    private boolean contextMenuShown = false;
    private GestureDetectorCompat gestureDetector;

    private ContextMenuCallback contextMenuCallback;

    public SSReadingView(final Context context) {
        super(context);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        this.addJavascriptInterface(new SSReadViewBridge(context), bridgeName);

    }

    public SSReadingView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        this.addJavascriptInterface(new SSReadViewBridge(context), bridgeName);
    }

    public SSReadingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new GestureDetectorCompat(context, new SSReadingView.GestureListener());
        this.addJavascriptInterface(new SSReadViewBridge(context), bridgeName);
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
            contextMenuCallback.onSelectionFinished();
            contextMenuShown = false;
            return true;
        }
    }

    private class SSReadViewBridge {
        Context context;
        SSReadViewBridge(Context c) {
            context = c;
        }

        @JavascriptInterface
        public void saveComments(String comments, final String inputId){
            try {
                comments = new String(Base64.decode(comments, Base64.DEFAULT), "UTF-8");

                new MaterialDialog.Builder(context)
                        .title("Comment")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Enter your comment", comments, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, final CharSequence input) {
                                ((SSReadingActivity)context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadUrl(String.format("javascript:ssReader.setComment('%s', '%s');", Base64.encodeToString(input.toString().getBytes(), Base64.DEFAULT), inputId));
                                    }
                                });


                            }
                        }).show();

            } catch (Exception e){}
        }
    }
}
