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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SSReadingViewModel implements SSViewModel {
    private static final String TAG = SSReadingViewModel.class.getSimpleName();
    private static final int ANIMATION_DURATION = 300;

    private Context context;
    private String ssLessonIndex;
    private DataListener dataListener;
    private SSLessonInfo ssLessonInfo;
    private SSRead ssRead;
    private DatabaseReference mDatabase;


    public SSReadingViewModel(Context context, DataListener dataListener, String ssLessonIndex) {
        this.context = context;
        this.dataListener = dataListener;
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

    public void onMenuClick(View view) {
        ((SSReadingActivity) context).c();
        // show reading list
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayout_marginTop(final View v, int topMargin) {
        int start = (topMargin > 0) ? 0: ((ViewGroup.MarginLayoutParams)v.getLayoutParams()).topMargin;
        int end = (topMargin > 0) ? (int) topMargin : 0;

        ValueAnimator slideAnimator = ValueAnimator.ofInt(start, end).setDuration(ANIMATION_DURATION);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
                layoutParams.topMargin = value;
                v.setLayoutParams(layoutParams);
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
