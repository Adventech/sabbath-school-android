package com.cryart.sabbathschool.behavior;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class SSReadingNavigationSheetBehavior<V extends View> extends BottomSheetBehavior<V> {
    private OnNestedScrollCallback _SSOnScrollChangedCallback;

    public SSReadingNavigationSheetBehavior() {}

    public SSReadingNavigationSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnNestedScrollCallback(OnNestedScrollCallback _SSOnScrollChangedCallback){
        this._SSOnScrollChangedCallback = _SSOnScrollChangedCallback;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (_SSOnScrollChangedCallback != null) {
            this._SSOnScrollChangedCallback.onNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        }
    }

    public static interface OnNestedScrollCallback {
        public void onNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed);
    }
}
