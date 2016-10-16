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

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.webkit.WebViewClient;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.behavior.SSReadingNavigationSheetBehavior;
import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.viewmodel.SSReadingViewModel;

public class SSReadingActivity extends SSBaseActivity  {
    private static final String TAG = SSReadingActivity.class.getSimpleName();

    private SsReadingActivityBinding binding;
    protected SSReadingViewModel ssReadingViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ss_reading_activity);

        binding.ssAppBar.ssAppBarLayout.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding.ssAppBar.ssToolbar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setSupportActionBar(binding.ssAppBar.ssToolbar);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        binding.ssWw.getSettings().setJavaScriptEnabled(true);
        binding.ssWw.setWebViewClient(new WebViewClient());
        binding.ssWw.loadUrl("https://s3-us-west-2.amazonaws.com/com.cryart.sabbathschool/sabbath-school-reader/index.html");

        SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior = (SSReadingNavigationSheetBehavior) ((CoordinatorLayout.LayoutParams) binding.bottomSheet.bottomSheet2.getLayoutParams()).getBehavior();
        ssReadingViewModel = new SSReadingViewModel(this, ssReadingNavigationSheetBehavior);

        if (ssReadingNavigationSheetBehavior != null) {
            ssReadingNavigationSheetBehavior.setOnNestedScrollCallback(ssReadingViewModel);
        }



        binding.executePendingBindings();
        binding.setViewModel(ssReadingViewModel);

        setUpDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLogoutEvent(){
        finish();
    }
}
