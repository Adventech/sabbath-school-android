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

package com.cryart.sabbathschool.view;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsReadingDisplayOptionsBinding;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;
import com.cryart.sabbathschool.viewmodel.SSReadingDisplayOptionsViewModel;
import com.cryart.sabbathschool.viewmodel.SSReadingViewModel;

public class SSReadingDisplayOptionsView extends BottomSheetDialogFragment {
    private SsReadingDisplayOptionsBinding binding;

    public void setSSReadingViewModel(Context context, SSReadingViewModel ssReadingViewModel, SSReadingDisplayOptions ssReadingDisplayOptions) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ss_reading_display_options, null, false);
        binding.setViewModel(new SSReadingDisplayOptionsViewModel(binding, ssReadingViewModel, ssReadingDisplayOptions));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (binding == null){
            this.binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.ss_reading_display_options, null, false);
        }
        return binding.getRoot();
    }
}
