/*
 * Copyright (c) 2017 Adventech.
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

package com.cryart.sabbathschool.lessons.ui.readings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.cryart.sabbathschool.core.misc.SSConstants;
import com.cryart.sabbathschool.reader.data.model.SSRead;
import com.cryart.sabbathschool.core.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.lessons.R;
import com.cryart.sabbathschool.lessons.data.model.SSReadComments;
import com.cryart.sabbathschool.lessons.data.model.SSReadHighlights;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SSReadingViewAdapter extends PagerAdapter {
    private final Context mContext;
    private final SSReadingViewModel ssReadingViewModel;
    public List<SSRead> ssReads;
    public List<SSReadHighlights> ssReadHighlights;
    public List<SSReadComments> ssReadComments;

    public SSReadingViewAdapter(Context context, SSReadingViewModel ssReadingViewModel) {
        mContext = context;
        this.ssReads = Collections.emptyList();
        this.ssReadComments = Collections.emptyList();
        this.ssReadHighlights = Collections.emptyList();
        this.ssReadingViewModel = ssReadingViewModel;
    }

    public void setSSReads(List<SSRead> ssReads) {
        this.ssReads = ssReads;
    }

    public void setSSReadHighlights(List<SSReadHighlights> ssReadHighlights) {
        this.ssReadHighlights = ssReadHighlights;
    }

    public void setSSReadComments(List<SSReadComments> ssReadComments) {
        this.ssReadComments = ssReadComments;
    }

    @Override
    @NotNull
    public Object instantiateItem(@NotNull ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.ss_reading_view, collection, false);
        collection.addView(layout);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        SSReadingDisplayOptions ssReadingDisplayOptions = new SSReadingDisplayOptions(
            prefs.getString(SSConstants.SS_SETTINGS_THEME_KEY, SSReadingDisplayOptions.SS_THEME_LIGHT),
            prefs.getString(SSConstants.SS_SETTINGS_SIZE_KEY, SSReadingDisplayOptions.SS_SIZE_MEDIUM),
            prefs.getString(SSConstants.SS_SETTINGS_FONT_KEY, SSReadingDisplayOptions.SS_FONT_LATO)
        );

        final SSReadingView ssReadingView = layout.findViewById(R.id.ss_reading_view);
        ssReadingView.setContextMenuCallback(ssReadingViewModel);
        ssReadingView.setHighlightsCommentsCallback(ssReadingViewModel);
        ssReadingView.setReadHighlights(ssReadHighlights.get(position));
        ssReadingView.setReadComments(ssReadComments.get(position));
        ssReadingView.loadContent(ssReads.get(position).getContent(), ssReadingDisplayOptions);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            ssReadingView.updateHighlights();
            ssReadingView.updateComments();
        }, 800);

        layout.setTag("ssReadingView_" + position);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NotNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return ssReads.size();
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }
}
