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

package com.cryart.sabbathschool.viewmodel;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.cryart.sabbathschool.behavior.SSReadingNavigationSheetBehavior;
import com.cryart.sabbathschool.misc.SSHelper;

public class SSReadingViewModel implements SSViewModel, SSReadingNavigationSheetBehavior.OnNestedScrollCallback {
    private static final int PEEK_HEIGHT = 60;

    private Context context;
    private SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior;

    public ObservableInt ssReadingNavigationSheetPeekHeight;


    public SSReadingViewModel(Context context, SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior){
        this.context = context;
        this.ssReadingNavigationSheetBehavior = ssReadingNavigationSheetBehavior;
        ssReadingNavigationSheetPeekHeight = new ObservableInt(SSHelper.convertDpToPixels(context, PEEK_HEIGHT));
    }

    @BindingAdapter("app:behavior_peekHeight")
    public static void setBehaviorPeekHeight(final View v, float peekHeight) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)v.getLayoutParams();
        final SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior = (SSReadingNavigationSheetBehavior)params.getBehavior();

        if (ssReadingNavigationSheetBehavior == null){
            return;
        }

        int start = (peekHeight > 0) ? 0: ssReadingNavigationSheetBehavior.getPeekHeight();
        int end = (peekHeight > 0) ? (int) peekHeight : 0;
        ValueAnimator slideAnimator = ValueAnimator.ofInt(start, end).setDuration(300);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ssReadingNavigationSheetBehavior.setPeekHeight(value);
                v.requestLayout();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(slideAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    public void destroy() {}

    public void onNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed){
        if (dyConsumed < 0) {
            ssReadingNavigationSheetPeekHeight.set(SSHelper.convertDpToPixels(context, PEEK_HEIGHT));
        } else if (dyConsumed > 0) {
            ssReadingNavigationSheetPeekHeight.set(0);
        }
    }

    public void onMenuClick(View view) {
        if (ssReadingNavigationSheetBehavior.getState() == SSReadingNavigationSheetBehavior.STATE_EXPANDED) {
            ssReadingNavigationSheetBehavior.setState(SSReadingNavigationSheetBehavior.STATE_COLLAPSED);
        } else {
            ssReadingNavigationSheetBehavior.setState(SSReadingNavigationSheetBehavior.STATE_EXPANDED);
        }
    }

}
