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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.adapter.SSLessonsAdapter;
import com.cryart.sabbathschool.databinding.SsQuarterlyActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.viewmodel.SSQuarterlyViewModel;
import com.mikepenz.materialdrawer.DrawerBuilder;

public class SSQuarterlyActivity extends AppCompatActivity implements SSQuarterlyViewModel.DataListener {
    private static final String TAG = SSQuarterlyActivity.class.getSimpleName();

    private SsQuarterlyActivityBinding binding;
    private SSQuarterlyViewModel ssQuarterlyViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ss_quarterly_activity);

        binding.ssAppBar.ssAppBarLayout.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding.ssAppBar.ssToolbar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        SSLessonsAdapter adapter = new SSLessonsAdapter();
        binding.ssLessonInfoList.setAdapter(adapter);
        binding.ssLessonInfoList.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.ssAppBar.ssToolbar);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        new DrawerBuilder().withActivity(this).build();

        ssQuarterlyViewModel = new SSQuarterlyViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_QUARTERLY_ID_EXTRA));
        binding.executePendingBindings();
        binding.setViewModel(ssQuarterlyViewModel);
    }

    @Override
    public void onQuarterlyChanged(SSQuarterly ssQuarterly) {
        setTitle(ssQuarterly.title);
        SSLessonsAdapter adapter = (SSLessonsAdapter) binding.ssLessonInfoList.getAdapter();
        adapter.setLessons(ssQuarterly.lessons);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ssQuarterlyViewModel.destroy();
    }
}
