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
import androidx.databinding.BaseObservable;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSLesson;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.cryart.sabbathschool.viewmodel.SSViewModel;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;

public class SSLessonItemViewModel extends BaseObservable implements SSViewModel {

    private SSLesson ssLesson;
    private Context context;

    SSLessonItemViewModel(Context context, SSLesson ssLesson) {
        this.ssLesson = ssLesson;
        this.context = context;
    }

    void setSsLesson(SSLesson ssLesson) {
        this.ssLesson = ssLesson;
        notifyChange();
    }

    public String getTitle() {
        return ssLesson.title;
    }

    public String getDate() {

        String startDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.start_date));

        String endDateOut = DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssLesson.end_date));

        return StringUtils.capitalize(startDateOut) + " - " + StringUtils.capitalize(endDateOut);
    }

    public void onItemClick() {
        Intent ssReadingIntent = new Intent(context, SSReadingActivity.class);
        ssReadingIntent.putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, ssLesson.index);
        context.startActivity(ssReadingIntent);
    }

    @Override
    public void destroy() {

    }
}

