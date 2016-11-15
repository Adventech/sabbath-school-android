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
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.ObservableInt;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSHelper;
import com.cryart.sabbathschool.model.SSComment;
import com.cryart.sabbathschool.model.SSContextMenu;
import com.cryart.sabbathschool.model.SSDay;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.model.SSReadComments;
import com.cryart.sabbathschool.model.SSReadHighlights;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.model.SSSuggestion;
import com.cryart.sabbathschool.view.SSBibleVersesActivity;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.cryart.sabbathschool.view.SSReadingDisplayOptionsView;
import com.cryart.sabbathschool.view.SSReadingView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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

    public ObservableInt ssLessonLoadingVisibility;
    public ObservableInt ssLessonEmptyStateVisibility;
    public ObservableInt ssLessonErrorStateVisibility;
    public ObservableInt ssLessonCoordinatorVisibility;

    public SSReadingViewModel(Context context, DataListener dataListener, String ssLessonIndex, SsReadingActivityBinding ssReadingActivityBinding) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssLessonIndex = ssLessonIndex;
        this.ssReadingActivityBinding = ssReadingActivityBinding;
        this.ssFirebaseAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        ssReadPosition = new ObservableInt(0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ssReadingDisplayOptions = new SSReadingDisplayOptions(
                prefs.getString(SSConstants.SS_SETTINGS_THEME_KEY, SSReadingDisplayOptions.SS_THEME_LIGHT),
                prefs.getString(SSConstants.SS_SETTINGS_SIZE_KEY, SSReadingDisplayOptions.SS_SIZE_MEDIUM),
                prefs.getString(SSConstants.SS_SETTINGS_FONT_KEY, SSReadingDisplayOptions.SS_FONT_ANDADA)
        );

        ssReadingActivityBinding.ssReadingView.setReadingDisplayOptions(ssReadingDisplayOptions);

        ssLessonLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonCoordinatorVisibility = new ObservableInt(View.INVISIBLE);

        loadLessonInfo();
    }

    private void loadLessonInfo(){
        ssLessonLoadingVisibility.set(View.VISIBLE);
        ssLessonEmptyStateVisibility.set(View.INVISIBLE);
        ssLessonErrorStateVisibility.set(View.INVISIBLE);
        ssLessonCoordinatorVisibility.set(View.INVISIBLE);
        
        mDatabase.child(SSConstants.SS_FIREBASE_LESSON_INFO_DATABASE)
                .child(ssLessonIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            ssLessonInfo = dataSnapshot.getValue(SSLessonInfo.class);
                            if (dataListener != null) dataListener.onLessonInfoChanged(ssLessonInfo);

                            if (ssLessonInfo != null && ssReadPosition != null && ssLessonInfo.days.size() > 0) {
                                DateTime today = DateTime.now().withTimeAtStartOfDay();
                                int idx = 0;

                                for (SSDay ssDay : ssLessonInfo.days){
                                    DateTime startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                                                .parseLocalDate(ssDay.date).toDateTimeAtStartOfDay();
                                    if (startDate.isEqual(today)){
                                        ssReadPosition.set(idx);
                                        break;
                                    }
                                    idx++;
                                }
                            }

                            ssLessonCoordinatorVisibility.set(View.VISIBLE);
                            ssLessonLoadingVisibility.set(View.INVISIBLE);
                            ssLessonEmptyStateVisibility.set(View.INVISIBLE);
                            ssLessonErrorStateVisibility.set(View.INVISIBLE);

                            loadRead();
                        } else {
                            ssLessonEmptyStateVisibility.set(View.VISIBLE);
                            ssLessonErrorStateVisibility.set(View.INVISIBLE);
                            ssLessonLoadingVisibility.set(View.INVISIBLE);
                            ssLessonCoordinatorVisibility.set(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ssLessonErrorStateVisibility.set(View.VISIBLE);
                        ssLessonLoadingVisibility.set(View.INVISIBLE);
                        ssLessonEmptyStateVisibility.set(View.INVISIBLE);
                        ssLessonCoordinatorVisibility.set(View.INVISIBLE);
                    }
                });
    }

    public void promptForEditSuggestion(){
        if (ssRead != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final String name = prefs.getString(SSConstants.SS_USER_NAME_INDEX, context.getString(R.string.ss_menu_anonymous_name));
            final String email = prefs.getString(SSConstants.SS_USER_EMAIL_INDEX, context.getString(R.string.ss_menu_anonymous_email));

            new MaterialDialog.Builder(context)
                    .title(context.getString(R.string.ss_reading_suggest_edit))
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                    .input(context.getString(R.string.ss_reading_suggest_edit_hint), "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {

                            mDatabase.child(SSConstants.SS_FIREBASE_SUGGESTIONS_DATABASE)
                                    .child(ssFirebaseAuth.getCurrentUser().getUid())
                                    .child(ssRead.index)
                                    .setValue(new SSSuggestion(name, email, input.toString()));

                            Toast.makeText(context, context.getString(R.string.ss_reading_suggest_edit_done), Toast.LENGTH_LONG).show();
                        }
                    }).show();
        }
    }

    private void loadRead(){
        if (ssLessonInfo != null && ssReadPosition != null) {
            String dayIndex = ssLessonInfo.days.get(ssReadPosition.get()).index;
            loadRead(dayIndex);
        }
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

                        if (ssReadingActivityBinding != null) {
                            ssReadingActivityBinding.ssReadingView.setReadHighlights(ssReadHighlights);
                            ssReadingActivityBinding.ssReadingView.updateHighlights();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        SSReadHighlights ssReadHighlights = new SSReadHighlights(dayIndex, "");
                        if (ssReadingActivityBinding != null) {
                            ssReadingActivityBinding.ssReadingView.setReadHighlights(ssReadHighlights);
                        }
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
                        if (ssReadingActivityBinding != null) {
                            ssReadingActivityBinding.ssReadingView.setReadComments(ssReadComments);
                            ssReadingActivityBinding.ssReadingView.updateComments();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        SSReadComments ssReadComments = new SSReadComments(dayIndex, new ArrayList<SSComment>());
                        if (ssReadingActivityBinding != null) {
                            ssReadingActivityBinding.ssReadingView.setReadComments(ssReadComments);
                        }
                    }
                });

        ssReadRef = mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
                .child(dayIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            ssRead = dataSnapshot.getValue(SSRead.class);
                            if (dataListener != null) dataListener.onReadChanged(ssRead);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void destroy() {
        context = null;
        dataListener = null;
        ssLessonInfo = null;
        ssLessonIndex = null;
        ssFirebaseAuth = null;

        if (ssReadRef != null){
            mDatabase.removeEventListener(ssReadRef);
        }

        if (ssHighlightsRef != null){
            mDatabase.removeEventListener(ssHighlightsRef);
        }

        if (ssCommentsRef != null){
            mDatabase.removeEventListener(ssCommentsRef);
        }

        mDatabase = null;
        ssReadRef = null;
        ssHighlightsRef = null;
        ssCommentsRef = null;
        ssReadingActivityBinding = null;
        ssReadingDisplayOptions = null;
        ssReadPosition = null;
        ssRead = null;
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
                            .parseLocalDate(ssLessonInfo.lesson.start_date));

            String endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                    .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                            .parseLocalDate(ssLessonInfo.lesson.end_date));

            return StringUtils.capitalize(startDateOut) + " - " + StringUtils.capitalize(endDateOut);
        }
        return "";
    }

    public String formatDate(String date, String DateFormatOutput){
        return StringUtils.capitalize(DateTimeFormat.forPattern(DateFormatOutput)
                .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(date)));
    }

    public String formatDate(String date){
        return formatDate(date, SSConstants.SS_DATE_FORMAT_OUTPUT);
    }

    public String getDayDate(int ssDayIdx) {
        if (ssLessonInfo != null && ssLessonInfo.days.size() > ssDayIdx){
            return formatDate(ssLessonInfo.days.get(ssDayIdx).date);
        }
        return "";
    }

    @Override
    public void onSelectionStarted(float x, float y) {
        if (ssReadingActivityBinding != null) {
            y = y - ssReadingActivityBinding.ssReadingViewScroll.getScrollY();

            DisplayMetrics metrics = new DisplayMetrics();
            ((SSReadingActivity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getLayoutParams();

            int contextMenuWidth = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getWidth();
            int contextMenuHeight = ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.getHeight();


            int screenWidth = metrics.widthPixels;
            int screenHeight = metrics.heightPixels;

            int margin = SSHelper.convertDpToPixels(context, 20);
            int jumpMargin = SSHelper.convertDpToPixels(context, 60);


            int contextMenuX = (int) x - (contextMenuWidth / 2);
            int contextMenuY = ssReadingActivityBinding.ssReadingViewScroll.getTop() + (int) y - contextMenuHeight - margin;

            if (contextMenuX - margin < 0) {
                contextMenuX = margin;
            }

            if (contextMenuX + contextMenuWidth + margin > screenWidth) {
                contextMenuX = screenWidth - margin - contextMenuWidth;
            }

            if (contextMenuY - margin < 0) {
                contextMenuY = contextMenuY + contextMenuHeight + jumpMargin;
            }

            params.setMargins(contextMenuX, contextMenuY, 0, 0);
            ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setLayoutParams(params);
            ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelectionFinished() {
        if (ssReadingActivityBinding != null) {
            ssReadingActivityBinding.ssContextMenu.ssReadingContextMenu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onHighlightsReceived(SSReadHighlights ssReadHighlights) {
        if (mDatabase != null){
            mDatabase.child(SSConstants.SS_FIREBASE_HIGHLIGHTS_DATABASE)
                    .child(ssFirebaseAuth.getCurrentUser().getUid())
                    .child(ssReadHighlights.readIndex)
                    .setValue(ssReadHighlights);
        }
    }

    @Override
    public void onCommentsReceived(SSReadComments ssReadComments) {
        if (mDatabase != null) {
            mDatabase.child(SSConstants.SS_FIREBASE_COMMENTS_DATABASE)
                    .child(ssFirebaseAuth.getCurrentUser().getUid())
                    .child(ssReadComments.readIndex)
                    .setValue(ssReadComments);
        }
    }

    @Override
    public void onVerseClicked(String verse) {
        Intent _SSBibleActivityIntent = new Intent(context, SSBibleVersesActivity.class);
        _SSBibleActivityIntent.putExtra(SSConstants.SS_READ_INDEX_EXTRA, ssRead.index);
        _SSBibleActivityIntent.putExtra(SSConstants.SS_READ_VERSE_EXTRA, verse);
        context.startActivity(_SSBibleActivityIntent);
    }

    public interface DataListener {
        void onLessonInfoChanged(SSLessonInfo ssLessonInfo);
        void onReadChanged(SSRead ssRead);
    }

    public void onDisplayOptionsClick(){
        SSReadingDisplayOptionsView ssReadingDisplayOptionsView = new SSReadingDisplayOptionsView();
        ssReadingDisplayOptionsView.setSSReadingViewModel(context, this, ssReadingDisplayOptions);
        ssReadingDisplayOptionsView.show(((SSReadingActivity)context).getSupportFragmentManager(), ssReadingDisplayOptionsView.getTag());
    }

    public void highlightYellow(){
        highlightSelection(SSContextMenu.HIGHLIGHT_YELLOW);
    }

    public void highlightOrange(){
        highlightSelection(SSContextMenu.HIGHLIGHT_ORANGE);
    }

    public void highlightGreen(){
        highlightSelection(SSContextMenu.HIGHLIGHT_GREEN);
    }

    public void highlightBlue(){
        highlightSelection(SSContextMenu.HIGHLIGHT_BLUE);
    }

    public void unHighlightSelection(){
        ssReadingActivityBinding.ssReadingView.ssReadViewBridge.unHighlightSelection();
        ssReadingActivityBinding.ssReadingView.selectionFinished();
    }

    private void highlightSelection(String color){
        ssReadingActivityBinding.ssReadingView.ssReadViewBridge.highlightSelection(color);
        ssReadingActivityBinding.ssReadingView.selectionFinished();
    }

    public void copy(){
        ssReadingActivityBinding.ssReadingView.ssReadViewBridge.copy();
        ssReadingActivityBinding.ssReadingView.selectionFinished();
    }

    public void share(){
        ssReadingActivityBinding.ssReadingView.ssReadViewBridge.share();
        ssReadingActivityBinding.ssReadingView.selectionFinished();
    }

    public void search(){
        ssReadingActivityBinding.ssReadingView.ssReadViewBridge.search();
        ssReadingActivityBinding.ssReadingView.selectionFinished();
    }

    public void onSSReadingDisplayOptions(SSReadingDisplayOptions ssReadingDisplayOptions){
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(SSConstants.SS_SETTINGS_THEME_KEY, ssReadingDisplayOptions.theme);
        editor.putString(SSConstants.SS_SETTINGS_FONT_KEY, ssReadingDisplayOptions.font);
        editor.putString(SSConstants.SS_SETTINGS_SIZE_KEY, ssReadingDisplayOptions.size);
        editor.apply();

        ssReadingActivityBinding.ssReadingView.setReadingDisplayOptions(ssReadingDisplayOptions);
        ssReadingActivityBinding.ssReadingView.updateReadingDisplayOptions();
    }
}
