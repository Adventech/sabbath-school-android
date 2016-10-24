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
import android.content.SharedPreferences;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSHelper;
import com.cryart.sabbathschool.model.SSComment;
import com.cryart.sabbathschool.model.SSDay;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.model.SSReadComments;
import com.cryart.sabbathschool.model.SSReadHighlights;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.cryart.sabbathschool.view.SSReadingDisplayOptionsView;
import com.cryart.sabbathschool.view.SSReadingView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

public class SSReadingViewModel implements SSViewModel, SSReadingView.ContextMenuCallback, SSReadingView.HighlightsCommentsCallback {
    private static final String TAG = SSReadingViewModel.class.getSimpleName();

    private Context context;
    private String ssLessonIndex;
    private DataListener dataListener;
    private FirebaseAuth ssFirebaseAuth;
    private DatabaseReference mDatabase;
    private ValueEventListener ssReadRef;
    private ValueEventListener ssHighlightsRef;
    private ValueEventListener ssCommentsRef;

    public SsReadingActivityBinding ssReadingActivityBinding;
    public SSReadingDisplayOptions ssReadingDisplayOptions;
    public SSLessonInfo ssLessonInfo;
    public ObservableInt ssReadPosition;
    public SSRead ssRead;

    public SSReadingViewModel(Context context, DataListener dataListener, String ssLessonIndex, SsReadingActivityBinding ssReadingActivityBinding) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssLessonIndex = ssLessonIndex;
        this.ssReadingActivityBinding = ssReadingActivityBinding;
        this.ssFirebaseAuth = FirebaseAuth.getInstance();

        ssReadingActivityBinding.ssContextMenu.contextMenuShare.setImageDrawable(new IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Color.DKGRAY)
                .sizeDp(16));

        ssReadingActivityBinding.ssContextMenu.contextMenuCopy.setImageDrawable(new IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_content_copy)
                .color(Color.DKGRAY)
                .sizeDp(16));

        ssReadingActivityBinding.ssContextMenu.contextMenuSearch.setImageDrawable(new IconicsDrawable(context)
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(Color.DKGRAY)
                .sizeDp(16));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        ssReadPosition = new ObservableInt(0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ssReadingDisplayOptions = new SSReadingDisplayOptions(
                prefs.getString(SSConstants.SS_SETTINGS_THEME_KEY, SSReadingDisplayOptions.SS_THEME_LIGHT),
                prefs.getString(SSConstants.SS_SETTINGS_SIZE_KEY, SSReadingDisplayOptions.SS_SIZE_MEDIUM),
                prefs.getString(SSConstants.SS_SETTINGS_FONT_KEY, SSReadingDisplayOptions.SS_FONT_LATO)
        );

        ssReadingActivityBinding.ssWw.setReadingDisplayOptions(ssReadingDisplayOptions);



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

    public void loadRead(final String dayIndex){
        if (ssReadRef != null){
            mDatabase.removeEventListener(ssReadRef);
        }

        if (ssHighlightsRef != null){
            mDatabase.removeEventListener(ssHighlightsRef);
        }

        if (ssCommentsRef != null){
            mDatabase.removeEventListener(ssCommentsRef);
        }

        ssHighlightsRef = mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
                .child(ssFirebaseAuth.getCurrentUser().getUid())
                .child(dayIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SSReadHighlights ssReadHighlights = new SSReadHighlights(dayIndex, "");
                        if (dataSnapshot != null) {
                            if (dataSnapshot.getValue(SSReadHighlights.class) != null){
                                ssReadHighlights = dataSnapshot.getValue(SSReadHighlights.class);
                            }
                        }
                        ssReadingActivityBinding.ssWw.setReadHighlights(ssReadHighlights);
                        ssReadingActivityBinding.ssWw.updateHighlights();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        SSReadHighlights ssReadHighlights = new SSReadHighlights(dayIndex, "");
                        ssReadingActivityBinding.ssWw.setReadHighlights(ssReadHighlights);
                    }
                });

        ssCommentsRef = mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
                .child(ssFirebaseAuth.getCurrentUser().getUid())
                .child(dayIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        SSReadComments ssReadComments = new SSReadComments(dayIndex, new ArrayList<SSComment>());
                        if (dataSnapshot != null) {
                            if (dataSnapshot.getValue(SSReadComments.class) != null){
                                ssReadComments = dataSnapshot.getValue(SSReadComments.class);
                            }
                        }
                        ssReadingActivityBinding.ssWw.setReadComments(ssReadComments);
                        ssReadingActivityBinding.ssWw.updateComments();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        SSReadComments ssReadComments = new SSReadComments(dayIndex, new ArrayList<SSComment>());
                        ssReadingActivityBinding.ssWw.setReadComments(ssReadComments);
                    }
                });

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
        final View view = ssReadingActivityBinding.ssReadingSheet;
        final int state = view.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int centerX = view.getRight() / 2;
            int centerY = view.getHeight() - 20;
            int startRadius = (state == View.VISIBLE) ? 0 : view.getHeight();
            int endRadius = (state == View.VISIBLE) ? view.getHeight() : 0;

            Animator anim = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);

            if (state == View.VISIBLE) {
                view.setVisibility(state);
                ssReadingActivityBinding.ssReadingSheetOverlay.setVisibility(state);

            } else {
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(state);
                        ssReadingActivityBinding.ssReadingSheetOverlay.setVisibility(state);
                    }
                });
            }
            anim.start();
        } else {
            view.setVisibility(state);
            ssReadingActivityBinding.ssReadingSheetOverlay.setVisibility(state);
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

    public String getLessonInterval() {
        if (ssLessonInfo != null){
            String startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseDateTime(ssLessonInfo.lesson.start_date));

            String endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseDateTime(ssLessonInfo.lesson.end_date));

            return startDateOut + " - " + endDateOut;
        }
        return "";
    }

    public String getDayDate(int ssDayIdx) {
        if (ssLessonInfo != null && ssLessonInfo.days.size() > ssDayIdx){
            return DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseDateTime(ssLessonInfo.days.get(ssDayIdx).date));
        }
        return "";
    }

    @Override
    public void onSelectionStarted(float x, float y) {
        y = y - ssReadingActivityBinding.nsv.getScrollY();

        DisplayMetrics metrics = new DisplayMetrics();
        ((SSReadingActivity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getLayoutParams();

        int contextMenuWidth = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getWidth();
        int contextMenuHeight = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getHeight();


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
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setLayoutParams(params);
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSelectionFinished() {
        ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onHighlightsReceived(SSReadHighlights ssReadHighlights) {
        mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
                .child(ssFirebaseAuth.getCurrentUser().getUid())
                .child(ssReadHighlights.readIndex)
                .setValue(ssReadHighlights);
    }

    @Override
    public void onCommentsReceived(SSReadComments ssReadComments) {
        mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
                .child(ssFirebaseAuth.getCurrentUser().getUid())
                .child(ssReadComments.readIndex)
                .setValue(ssReadComments);
    }

    public interface DataListener {
        void onLessonInfoChanged(SSLessonInfo ssLessonInfo);
        void onReadChanged(SSRead ssRead);
    }

    public void onDisplayOptionsClick(){
        SSReadingDisplayOptionsView ssReadingDisplayOptionsView = new SSReadingDisplayOptionsView();
        Log.d(TAG, ssReadingDisplayOptions.size);
        ssReadingDisplayOptionsView.setSSReadingViewModel(context, this, ssReadingDisplayOptions);
        ssReadingDisplayOptionsView.show(((SSReadingActivity)context).getSupportFragmentManager(), ssReadingDisplayOptionsView.getTag());
    }

    public void highlightYellow(){
        highlightSelection("yellow");
    }

    public void highlightOrange(){
        highlightSelection("orange");
    }

    public void highlightGreen(){
        highlightSelection("green");
    }

    public void highlightBlue(){
        highlightSelection("blue");
    }

    public void unHighlightSelection(){
        ssReadingActivityBinding.ssWw.ssReadViewBridge.unHighlightSelection();
        ssReadingActivityBinding.ssWw.selectionFinished();
    }

    private void highlightSelection(String color){
        ssReadingActivityBinding.ssWw.ssReadViewBridge.highlightSelection(color);
        ssReadingActivityBinding.ssWw.selectionFinished();
    }

    public void copy(){
        ssReadingActivityBinding.ssWw.ssReadViewBridge.copy();
        ssReadingActivityBinding.ssWw.selectionFinished();
    }

    public void share(){
        ssReadingActivityBinding.ssWw.ssReadViewBridge.share();
        ssReadingActivityBinding.ssWw.selectionFinished();
    }

    public void search(){
        ssReadingActivityBinding.ssWw.ssReadViewBridge.search();
        ssReadingActivityBinding.ssWw.selectionFinished();
    }

    public void onSSReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions){
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(SSConstants.SS_SETTINGS_THEME_KEY, ssReadingDisplayOptions.theme);
        editor.putString(SSConstants.SS_SETTINGS_FONT_KEY, ssReadingDisplayOptions.font);
        editor.putString(SSConstants.SS_SETTINGS_SIZE_KEY, ssReadingDisplayOptions.size);
        editor.apply();

        ssReadingActivityBinding.ssWw.setReadingDisplayOptions(ssReadingDisplayOptions);
        ssReadingActivityBinding.ssWw.updateReadingDisplayOptions();
    }
}
