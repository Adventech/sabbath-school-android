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
import android.databinding.BindingAdapter;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.bus.SSBusProvider;
import com.cryart.sabbathschool.event.SSLanguageFilterChangeEvent;
import com.cryart.sabbathschool.model.SSQuarterlyLanguage;
import com.squareup.otto.Produce;

import mehdi.sakout.fancybuttons.FancyButton;

public class SSQuarterlyLanguageItemViewModel extends BaseObservable implements SSViewModel {
    private static final String TAG = SSQuarterlyLanguageItemViewModel.class.getSimpleName();
    private SSQuarterlyLanguage ssQuarterlyLanguage;
    private Context context;
    private SSQuarterliesLanguageChangeListener changeListener;

    public SSQuarterlyLanguageItemViewModel(Context context, SSQuarterlyLanguage ssQuarterlyLanguage) {
        this.ssQuarterlyLanguage = ssQuarterlyLanguage;
        this.context = context;
    }

    public void setChangeListener(SSQuarterliesLanguageChangeListener changeListener){
        this.changeListener = changeListener;
    }

    public void setSsQuarterlyLanguage(SSQuarterlyLanguage ssQuarterlyLanguage) {
        this.ssQuarterlyLanguage = ssQuarterlyLanguage;
        notifyChange();
    }

    public String getCode() {
        return ssQuarterlyLanguage.code;
    }

    public String getName() {
        return ssQuarterlyLanguage.name;
    }

    public int getSelected() {
        return ssQuarterlyLanguage.selected;
    }

    @BindingAdapter("fancy:fb_text")
    public static void setfbText(View v, String n) {
        ((FancyButton)v).setText(n);
    }

    @BindingAdapter("fancyColor")
    public static void setFancyColor(View v, int selected){
        FancyButton view = (FancyButton) v;
        if (selected == 1) {
            view.setFocusBackgroundColor(R.color.ss_quarterly_item_featured_read_focus_color);
            view.setBackgroundColor(
                    ContextCompat.getColor(v.getContext(), R.color.ss_quarterly_item_featured_read_default_color)
            );
        } else {
            view.setFocusBackgroundColor(R.color.ss_quarterly_item_featured_read_default_color);
            view.setBackgroundColor(
                    ContextCompat.getColor(v.getContext(), R.color.ss_quarterly_item_featured_read_focus_color)
            );
        }
    }

    public void onClick(View view) {
        ssQuarterlyLanguage.selected = 1;
        changeListener.onLanguageCheck(ssQuarterlyLanguage.code);
        notifyChange();

        SSBusProvider.getInstance().post(produceEvent());
    }

    @Override
    public void destroy() {

    }

    public interface SSQuarterliesLanguageChangeListener {
        boolean onLanguageCheck(String code);
    }

    @Produce
    public SSLanguageFilterChangeEvent produceEvent(){
        return new SSLanguageFilterChangeEvent();
    }
}
