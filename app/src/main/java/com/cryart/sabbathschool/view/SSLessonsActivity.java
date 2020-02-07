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
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.adapter.SSLessonsAdapter;
import com.cryart.sabbathschool.databinding.SsLessonsActivityBinding;
import com.cryart.sabbathschool.misc.SSColorTheme;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSQuarterlyInfo;
import com.cryart.sabbathschool.viewmodel.SSLessonsViewModel;

import hotchemi.android.rate.AppRate;

public class SSLessonsActivity extends SSBaseActivity implements SSLessonsViewModel.DataListener {
    private static final String TAG = SSLessonsActivity.class.getSimpleName();

    private SsLessonsActivityBinding binding;
    private SSLessonsViewModel ssLessonsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppRate.with(this).setInstallDays(SSConstants.SS_APP_RATE_INSTALL_DAYS).monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        binding = DataBindingUtil.setContentView(this, R.layout.ss_lessons_activity);

        SSLessonsAdapter adapter = new SSLessonsAdapter();
        binding.ssLessonInfoList.setAdapter(adapter);
        binding.ssLessonInfoList.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.ssLessonsAppBar.ssLessonsToolbar);

        ActionBar ssToolbar = getSupportActionBar();

        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setCollapsedTitleTextAppearance(R.style.AppThemeAppBarTextStyle);
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setExpandedTitleTextAppearance(R.style.AppThemeAppBarTextStyleExpanded);

        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setCollapsedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Bold.ttf"));
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setExpandedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Bold.ttf"));

        ssLessonsViewModel = new SSLessonsViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_QUARTERLY_INDEX_EXTRA));
        binding.executePendingBindings();
        binding.setViewModel(ssLessonsViewModel);

        setUpDrawer();
    }

    public void updateColorScheme(){
        int primaryColor = Color.parseColor(SSColorTheme.getInstance().getColorPrimary());
        binding.ssLessonsAppBar.ssLessonsToolbar.setBackgroundColor(primaryColor);
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setContentScrimColor(primaryColor);
        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setBackgroundColor(primaryColor);
        binding.ssLessonsAppBar.ssLessonsAppBarRead.setFocusBackgroundColor(primaryColor);
        binding.ssLessonsAppBar.ssLessonsAppBarRead.setBackgroundColor(Color.parseColor(SSColorTheme.getInstance().getColorPrimaryDark()));

        updateWindowColorScheme();
    }

    @Override
    public void onQuarterlyChanged(SSQuarterlyInfo ssQuarterlyInfo) {
        SSColorTheme.getInstance().setColorPrimary(ssQuarterlyInfo.quarterly.color_primary);
        SSColorTheme.getInstance().setColorPrimaryDark(ssQuarterlyInfo.quarterly.color_primary_dark);
        updateColorScheme();

        binding.ssLessonsAppBar.ssLessonCollapsingToolbar.setTitle(ssQuarterlyInfo.quarterly.title);
        SSLessonsAdapter adapter = (SSLessonsAdapter) binding.ssLessonInfoList.getAdapter();
        adapter.setLessons(ssQuarterlyInfo.lessons);
        adapter.notifyDataSetChanged();
        binding.invalidateAll();
        binding.executePendingBindings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.ss_lessons_menu, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ss_lessons_menu_share){
            shareApp(ssLessonsViewModel.ssQuarterlyInfo.quarterly.title);
        } else if (id == R.id.ss_lessons_menu_settings){
            onSettingsClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
