/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.lessons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import androidx.core.view.ViewCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableInt;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSLesson;
import com.cryart.sabbathschool.model.SSQuarterlyInfo;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.cryart.sabbathschool.viewmodel.SSViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

public class SSLessonsViewModel implements SSViewModel {

    private Context context;
    public SSQuarterlyInfo ssQuarterlyInfo;
    private String ssQuarterlyIndex;
    private DataListener dataListener;
    private DatabaseReference mDatabase;

    public ObservableInt ssLessonsLoadingVisibility;
    public ObservableInt ssLessonsEmptyStateVisibility;
    public ObservableInt ssLessonsErrorStateVisibility;
    public ObservableInt ssLessonsCoordinatorVisibility;
    private SharedPreferences shared;

    public SSLessonsViewModel(Context context, DataListener dataListener, String ssQuarterlyIndex) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssQuarterlyIndex = ssQuarterlyIndex;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        ssLessonsLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsCoordinatorVisibility = new ObservableInt(View.INVISIBLE);

        shared = PreferenceManager.getDefaultSharedPreferences(context);

        loadQuarterlyInfo(true);
    }

    public void setSsQuarterlyIndex(String ssQuarterlyIndex) {
        this.ssQuarterlyIndex = ssQuarterlyIndex;
        loadQuarterlyInfo(false);
    }

    private void loadQuarterlyInfo(boolean showLoading) {
        if (showLoading) {
            ssLessonsLoadingVisibility.set(View.VISIBLE);
        }
        ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
        ssLessonsErrorStateVisibility.set(View.INVISIBLE);
        mDatabase.child(SSConstants.SS_FIREBASE_QUARTERLY_INFO_DATABASE)
                .child(ssQuarterlyIndex)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        ssQuarterlyInfo = dataSnapshot.getValue(SSQuarterlyInfo.class);

                        if (shared != null) {
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString(SSConstants.SS_LAST_QUARTERLY_INDEX, ssQuarterlyInfo.quarterly.index);
                            editor.apply();
                        }

                        if (dataListener != null)
                            dataListener.onQuarterlyChanged(ssQuarterlyInfo);
                        ssLessonsLoadingVisibility.set(View.INVISIBLE);
                        ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
                        ssLessonsErrorStateVisibility.set(View.INVISIBLE);
                        ssLessonsCoordinatorVisibility.set(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {
                        ssLessonsLoadingVisibility.set(View.INVISIBLE);
                        ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
                        ssLessonsCoordinatorVisibility.set(View.INVISIBLE);
                        ssLessonsErrorStateVisibility.set(View.VISIBLE);
                    }
                });
    }

    @Override
    public void destroy() {
        context = null;
        dataListener = null;
        ssQuarterlyInfo = null;
        ssQuarterlyIndex = null;
        shared = null;
    }

    public void onReadClick() {
        if (ssQuarterlyInfo != null && ssQuarterlyInfo.lessons.size() > 0) {
            DateTime today = DateTime.now();
            String ssLessonIndex = ssQuarterlyInfo.lessons.get(0).index;

            for (SSLesson ssLesson : ssQuarterlyInfo.lessons) {
                DateTime startDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.start_date).toDateTimeAtStartOfDay();

                DateTime endDate = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.end_date).plusDays(1).toDateTimeAtStartOfDay().plusHours(12);

                if (startDate.isBefore(endDate) && new Interval(startDate, endDate).contains(today)) {
                    ssLessonIndex = ssLesson.index;
                    break;
                }
            }

            Intent ssReadingIntent = new Intent(context, SSReadingActivity.class);
            ssReadingIntent.putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, ssLessonIndex);
            context.startActivity(ssReadingIntent);
        }
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }


    public String getDate() {
        if (ssQuarterlyInfo != null) {
            return ssQuarterlyInfo.quarterly.human_date;
        }
        return "";
    }

    public String getDescription() {
        if (ssQuarterlyInfo != null) {
            return ssQuarterlyInfo.quarterly.description;
        }
        return "";
    }

    public String getCover() {
        if (ssQuarterlyInfo != null) {
            return ssQuarterlyInfo.quarterly.cover;
        }
        return "";
    }

    @BindingAdapter({"coverUrl"})
    public static void loadCover(ImageView view, String coverUrl) {
        ViewCompat.setElevation(view, 15.0f);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(coverUrl, view);
    }

    public interface DataListener {
        void onQuarterlyChanged(SSQuarterlyInfo ssQuarterlyInfo);
    }
}