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
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.adapter.SSLessonsAdapter;
import com.cryart.sabbathschool.databinding.SsLessonsActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSQuarterlyInfo;
import com.cryart.sabbathschool.viewmodel.SSLessonsViewModel;

public class SSLessonsActivity extends SSBaseActivity implements SSLessonsViewModel.DataListener {
    private static final String TAG = SSLessonsActivity.class.getSimpleName();

    private SsLessonsActivityBinding binding;
    private SSLessonsViewModel ssLessonsViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.ss_lessons_activity);

        SSLessonsAdapter adapter = new SSLessonsAdapter();
        binding.ssLessonInfoList.setAdapter(adapter);
        binding.ssLessonInfoList.setLayoutManager(new LinearLayoutManager(this));

        setUpDrawer();

        setSupportActionBar(binding.ssAppBar2.ssToolbar2);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {

            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }




        ssLessonsViewModel = new SSLessonsViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_QUARTERLY_PATH_EXTRA));
        binding.executePendingBindings();
        binding.setViewModel(ssLessonsViewModel);
    }

    @Override
    public void onQuarterlyChanged(SSQuarterlyInfo ssQuarterlyInfo) {
        binding.ssAppBar2.ssCollapsingToolbar.setTitle(ssQuarterlyInfo.quarterly.title);
        SSLessonsAdapter adapter = (SSLessonsAdapter) binding.ssLessonInfoList.getAdapter();
        adapter.setLessons(ssQuarterlyInfo.lessons);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ss_lessons_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ssLessonsViewModel.destroy();
    }

    @Override
    public void onLogoutEvent(){
        finish();
    }
}
