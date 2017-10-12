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

package com.cryart.sabbathschool.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.view.SSReadingView;
import com.cryart.sabbathschool.viewmodel.SSReadingViewModel;

import java.util.Collections;
import java.util.List;

public class SSReadingViewAdapter extends PagerAdapter {
    private Context mContext;
    public List<SSRead> ssReads;
    private SSReadingViewModel ssReadingViewModel;

    public SSReadingViewAdapter(Context context, SSReadingViewModel ssReadingViewModel) {
        mContext = context;
        this.ssReads = Collections.emptyList();
        this.ssReadingViewModel = ssReadingViewModel;
    }

    public void setSSReads(List<SSRead> ssReads){
        this.ssReads = ssReads;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
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
        ssReadingView.setReadingDisplayOptions(ssReadingDisplayOptions);
        ssReadingView.setContextMenuCallback(ssReadingViewModel);
        ssReadingView.setHighlightsCommentsCallback(ssReadingViewModel);
        ssReadingView.loadRead(ssReads.get(position));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ssReadingView != null) {
                   ssReadingView.updateHighlights();
                   ssReadingView.updateComments();
                }
            }
        }, 800);

        layout.setTag("ssReadingView_"+position);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return ssReads.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}