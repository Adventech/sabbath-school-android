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

import android.content.Context;
import android.databinding.BaseObservable;

import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSDay;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;

public class SSReadingListItemViewModel extends BaseObservable implements SSViewModel {
    private static final String TAG = SSReadingListItemViewModel.class.getSimpleName();
    private SSDay ssDay;
    private Context context;
    private SSReadingViewModel ssReadingViewModel;

    public SSReadingListItemViewModel(Context context, SSDay ssDay, SSReadingViewModel ssReadingViewModel) {
        this.ssDay = ssDay;
        this.context = context;
        this.ssReadingViewModel = ssReadingViewModel;
    }

    public void setSSDay(SSDay ssDay) {
        this.ssDay = ssDay;
        notifyChange();
    }

    public String getTitle() {
        return ssDay.title;
    }

    public String getDate() {
        return StringUtils.capitalize(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT_OUTPUT)
                .print(DateTimeFormat.forPattern(SSConstants.SS_DATE_FORMAT)
                        .parseLocalDate(ssDay.date)));
    }

    public void onItemClick(){
        ssReadingViewModel.loadRead(ssDay.index);
        ssReadingViewModel.onMenuClick();
        ssReadingViewModel.ssReadPosition.set(ssReadingViewModel.ssLessonInfo.days.indexOf(ssDay));
    }

    @Override
    public void destroy() {

    }
}
