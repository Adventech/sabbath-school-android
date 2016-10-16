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
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.view.SSLessonsActivity;

public class SSQuarterlyItemViewModel extends BaseObservable implements SSViewModel {
    private SSQuarterly ssQuarterly;
    private Context context;

    public SSQuarterlyItemViewModel(Context context, SSQuarterly ssQuarterly) {
        this.ssQuarterly = ssQuarterly;
        this.context = context;
    }

    public void setSsQuarterly(SSQuarterly ssQuarterly) {
        this.ssQuarterly = ssQuarterly;
        notifyChange();
    }

    public String getTitle() {
        return ssQuarterly.title;
    }

    public String getDate() {
        return ssQuarterly.date;
    }

    public String getCover() {
        return ssQuarterly.cover;
    }

    public String getDescription() {
        return ssQuarterly.description;
    }

    @BindingAdapter({"coverUrl"})
    public static void loadCover(ImageView view, String coverUrl) {
        ViewCompat.setElevation(view, 15.0f);
        Glide.with(view.getContext())
                .load(coverUrl)
                .into(view);
    }

    public void onReadClick(View view) {
        Intent ssLessonsIntent = new Intent(context, SSLessonsActivity.class);
        ssLessonsIntent.putExtra(SSConstants.SS_QUARTERLY_PATH_EXTRA, ssQuarterly.path);
        context.startActivity(ssLessonsIntent);
    }

    @Override
    public void destroy() {

    }
}
