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
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSHelper;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SSReadingViewModel implements SSViewModel, SSReadingNavigationSheetBehavior.OnNestedScrollCallback {
    private static final String TAG = SSReadingViewModel.class.getSimpleName();
    private static final int PEEK_HEIGHT = 60;

    private Context context;
    private String ssLessonIndex;
    private DataListener dataListener;
    private SSLessonInfo ssLessonInfo;
    private SSRead ssRead;
    private DatabaseReference mDatabase;

    private SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior;
    public ObservableInt ssReadingNavigationSheetPeekHeight;


    public SSReadingViewModel(Context context, DataListener dataListener, SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior, String ssLessonIndex) {
        ssReadingNavigationSheetPeekHeight = new ObservableInt(SSHelper.convertDpToPixels(context, PEEK_HEIGHT));

        this.context = context;
        this.dataListener = dataListener;
        this.ssReadingNavigationSheetBehavior = ssReadingNavigationSheetBehavior;
        this.ssLessonIndex = ssLessonIndex;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        loadLessonInfo();
    }

    private void loadLessonInfo(){
        mDatabase.child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
                .child(ssLessonIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            ssLessonInfo = dataSnapshot.getValue(SSLessonInfo.class);
                            dataListener.onLessonInfoChanged(ssLessonInfo);
                            loadRead(ssLessonInfo.days.get(0).index);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadRead(String dayIndex){
        mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
                .child(dayIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            ssRead = dataSnapshot.getValue(SSRead.class);
                            dataListener.onReadChanged(ssRead);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void destroy() {
        context = null;
        ssLessonInfo = null;
        dataListener = null;
    }

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

    public interface DataListener {
        void onLessonInfoChanged(SSLessonInfo ssLessonInfo);
        void onReadChanged(SSRead ssRead);
    }
}
