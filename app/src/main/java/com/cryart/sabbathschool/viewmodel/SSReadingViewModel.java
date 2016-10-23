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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.databinding.ObservableInt;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSHelper;
import com.cryart.sabbathschool.model.SSDay;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.cryart.sabbathschool.view.SSReadingDisplayOptionsView;
import com.cryart.sabbathschool.view.SSReadingView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class SSReadingViewModel implements SSViewModel, SSReadingView.ContextMenuCallback {
    private static final String TAG = SSReadingViewModel.class.getSimpleName();

    private Context context;
    private String ssLessonIndex;
    private DataListener dataListener;
    private DatabaseReference mDatabase;
    private ValueEventListener ssReadRef;
    private SsReadingActivityBinding ssReadingActivityBinding;

    public SSLessonInfo ssLessonInfo;
    public ObservableInt ssReadPosition;
    public SSRead ssRead;

    public SSReadingViewModel(Context context, DataListener dataListener, String ssLessonIndex, SsReadingActivityBinding ssReadingActivityBinding) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssLessonIndex = ssLessonIndex;
        this.ssReadingActivityBinding = ssReadingActivityBinding;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        ssReadPosition = new ObservableInt(0);

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

                            if (ssLessonInfo != null && ssLessonInfo.days.size() > 0) {
                                DateTime today = DateTime.now().withTimeAtStartOfDay();
                                int idx = 0;

                                for (SSDay ssDay : ssLessonInfo.days){
                                    DateTime startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                                            .parseDateTime(ssDay.date).withTimeAtStartOfDay();

                                    if (startDate.isEqual(today)){
                                        ssReadPosition.set(idx);
                                        break;
                                    }
                                    idx++;
                                }
                            }
                            loadRead();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadRead(){
        String dayIndex = ssLessonInfo.days.get(ssReadPosition.get()).index;
        loadRead(dayIndex);
    }

    public void loadRead(String dayIndex){
        if (ssReadRef != null){
            mDatabase.removeEventListener(ssReadRef);
        }

        ssReadRef = mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
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

    public void onMenuClick() {
        final View view = ((SSReadingActivity)context).findViewById(R.id.ss_reading_sheet);
        final int state = view.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int centerX = view.getRight() / 2;
            int centerY = view.getHeight() - 20;
            int startRadius = (state == View.VISIBLE) ? 0 : view.getHeight();
            int endRadius = (state == View.VISIBLE) ? view.getHeight() : 0;

            Animator anim = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);

            if (state == View.VISIBLE) {
                view.setVisibility(state);

            } else {
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(state);
                    }
                });
            }
            anim.start();
        } else {
            view.setVisibility(state);
        }
    }

    public void onNextClick(){
        ssReadPosition.set((ssReadPosition.get() < ssLessonInfo.days.size() - 1) ? ssReadPosition.get()+1 : ssReadPosition.get());
        loadRead();
    }

    public void onPrevClick(){
        ssReadPosition.set((ssReadPosition.get() > 0) ? ssReadPosition.get()-1 : ssReadPosition.get());
        loadRead();
    }

    public String getCover() {
        if (ssLessonInfo != null){
            return ssLessonInfo.lesson.cover;
        }
        return "";
    }

    @Override
    public void onSelectionStarted(float x, float y) {
        y = y - ssReadingActivityBinding.nsv.getScrollY();

        DisplayMetrics metrics = new DisplayMetrics();
        ((SSReadingActivity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) ssReadingActivityBinding.ssContextMenu.getLayoutParams();

        int contextMenuWidth = ssReadingActivityBinding.ssContextMenu.getWidth();
        int contextMenuHeight = ssReadingActivityBinding.ssContextMenu.getHeight();


        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int margin = SSHelper.convertDpToPixels(context, 20);
        int jumpMargin = SSHelper.convertDpToPixels(context, 60);


        int contextMenuX = (int)x - (contextMenuWidth / 2);
        int contextMenuY = ssReadingActivityBinding.nsv.getTop() + (int)y - contextMenuHeight - margin;

        if (contextMenuX - margin < 0){
            contextMenuX = margin;
        }

        if (contextMenuX + contextMenuWidth + margin > screenWidth){
            contextMenuX = screenWidth - margin - contextMenuWidth;
        }

        if (contextMenuY - margin < 0){
            contextMenuY = contextMenuY + contextMenuHeight + jumpMargin;
        }

        params.setMargins(contextMenuX, contextMenuY, 0, 0);
        ssReadingActivityBinding.ssContextMenu.setLayoutParams(params);
        ssReadingActivityBinding.ssContextMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSelectionFinished() {
        ssReadingActivityBinding.ssContextMenu.setVisibility(View.INVISIBLE);
    }

    public interface DataListener {
        void onLessonInfoChanged(SSLessonInfo ssLessonInfo);
        void onReadChanged(SSRead ssRead);
    }

    public void onDisplayOptionsClick(){
        SSReadingDisplayOptionsView ssReadingDisplayOptionsView = new SSReadingDisplayOptionsView();
        ssReadingDisplayOptionsView.setSSReadingViewModel(context, this);
        ssReadingDisplayOptionsView.show(((SSReadingActivity)context).getSupportFragmentManager(), ssReadingDisplayOptionsView.getTag());
    }

    public void onHideWebViewClick(){
        ssReadingActivityBinding.ssWw.setVisibility(View.INVISIBLE);
    }
}
