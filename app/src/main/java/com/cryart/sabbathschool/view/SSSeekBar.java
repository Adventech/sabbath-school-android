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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.cryart.sabbathschool.R;

public class SSSeekBar extends SeekBar {
    private static final int STROKE_WIDTH_IN_DP = 3;
    Paint mPaint;

    public SSSeekBar(Context context) {
        super(context);
        initPaint();
    }

    public SSSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public SSSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect progressRect = getProgressDrawable().getBounds();
        int offset = progressRect.width() / 4;

        int[] offsets = new int[]{(2 * -offset), -offset, 0, offset, 2 * offset};

        for (int index = 0; index < offsets.length; index++) {
            if (index != getProgress()) {
                drawSeekBarProgressIndicators(canvas, offsets[index]);
            }
        }
    }

    private void drawSeekBarProgressIndicators(Canvas canvas, int offset) {
        int XOriginPoint = getWidth() / 2;
        int YOriginPoint = getHeight() / 2;

        canvas.drawCircle(
                XOriginPoint + offset,
                YOriginPoint,
                getPxFromDp(STROKE_WIDTH_IN_DP),
                mPaint);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor((getResources().getColor(R.color.accent)));
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setStrokeWidth(getPxFromDp(STROKE_WIDTH_IN_DP));
        mPaint.setAntiAlias(true);
    }

    private float getPxFromDp(int valueInDp) {
        return getResources().getDisplayMetrics().density * valueInDp;
    }

}
