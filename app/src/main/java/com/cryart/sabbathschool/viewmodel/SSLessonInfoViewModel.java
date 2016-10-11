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
import android.util.Log;
import android.view.View;

import com.cryart.sabbathschool.model.SSLessonInfo;

public class SSLessonInfoViewModel extends BaseObservable implements SSViewModel {
    private static final String TAG = SSLessonInfoViewModel.class.getSimpleName();
    private SSLessonInfo ssLessonInfo;
    private Context context;

    public SSLessonInfoViewModel(Context context, SSLessonInfo ssLessonInfo) {
        this.ssLessonInfo = ssLessonInfo;
        this.context = context;
    }

    public void setSsLessonInfo(SSLessonInfo ssLessonInfo) {
        this.ssLessonInfo = ssLessonInfo;
        notifyChange();
    }

    public String getTitle() {
        return ssLessonInfo.title;
    }

    public String getDate() {
        return ssLessonInfo.date;
    }

    public void onItemClick(View v){
        Log.d(TAG, "Click");
    }


    @Override
    public void destroy() {

    }
}

