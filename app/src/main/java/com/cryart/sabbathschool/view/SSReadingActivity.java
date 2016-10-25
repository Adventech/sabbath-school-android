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
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.adapter.SSReadingListAdapter;
import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.viewmodel.SSReadingViewModel;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

public class SSReadingActivity extends SSBaseActivity implements SSReadingViewModel.DataListener {
    private static final String TAG = SSReadingActivity.class.getSimpleName();

    private SsReadingActivityBinding binding;
    protected SSReadingViewModel ssReadingViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ss_reading_activity);

        SSReadingListAdapter adapter = new SSReadingListAdapter();
        binding.ssReadingSheetList.setAdapter(adapter);
        binding.ssReadingSheetList.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.ssReadingAppBar.ssReadingToolbar);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setCollapsedTitleTextAppearance(R.style.AppThemeAppBarTextStyle);
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setExpandedTitleTextAppearance(R.style.AppThemeAppBarTextStyleExpanded);

        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setCollapsedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/PTF76F.ttf"));
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setExpandedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/PTF76F.ttf"));

        ViewCompat.setNestedScrollingEnabled(binding.ssReadingSheetList, false);

        ssReadingViewModel = new SSReadingViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_LESSON_INDEX_EXTRA), binding);
        binding.ssReadingView.setContextMenuCallback(ssReadingViewModel);
        binding.ssReadingView.setHighlightsCommentsCallback(ssReadingViewModel);
        ((SSReadingListAdapter)binding.ssReadingSheetList.getAdapter()).setReadingViewModel(ssReadingViewModel);

        binding.executePendingBindings();
        binding.setViewModel(ssReadingViewModel);

        setUpDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ss_reading_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.ss_reading_menu_display_options);
        menuItem.setIcon(
                new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_text_format)
                        .color(Color.WHITE)
                        .sizeDp(16)
        );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ss_reading_menu_display_options){
            binding.getViewModel().onDisplayOptionsClick();
        } else if (id == R.id.ss_reading_menu_settings){
            onSettingsClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLogoutEvent(){
        finish();
    }

    @Override
    public void onLessonInfoChanged(SSLessonInfo ssLessonInfo){
        final SSReadingListAdapter adapter = (SSReadingListAdapter) binding.ssReadingSheetList.getAdapter();
        adapter.setDays(ssLessonInfo.days);

        Glide.with(this)
                .load(ssLessonInfo.lesson.cover)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        adapter.notifyDataSetChanged();
                        binding.invalidateAll();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        resource.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.OVERLAY);
                        adapter.notifyDataSetChanged();
                        binding.invalidateAll();
                        return false;
                    }
                })
                .into(binding.ssReadingAppBar.ssCollapsingToolbarBackdrop);
    }

    @Override
    public void onReadChanged(SSRead ssRead){
        binding.ssReadingView.loadRead(ssRead);
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setTitle(ssRead.title);
    }

    @Override
    public void onBackPressed() {
        if (binding.ssReadingSheet.getVisibility() == View.VISIBLE){
            binding.getViewModel().onMenuClick();
        } else {
            super.onBackPressed();
        }
    }
}
