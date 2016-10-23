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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebViewClient;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

        setSupportActionBar(binding.ssAppBar.ssToolbar3);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        binding.ssAppBar.ssCollapsingToolbar.setCollapsedTitleTextAppearance(R.style.AppThemeAppBarTextStyle);
        binding.ssAppBar.ssCollapsingToolbar.setExpandedTitleTextAppearance(R.style.AppThemeAppBarTextStyleExpanded);

        binding.ssAppBar.ssCollapsingToolbar.setCollapsedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/PTF76F.ttf"));
        binding.ssAppBar.ssCollapsingToolbar.setExpandedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/PTF76F.ttf"));

        binding.ssWw.getSettings().setJavaScriptEnabled(true);
        binding.ssWw.setWebViewClient(new WebViewClient());

        ViewCompat.setNestedScrollingEnabled(binding.ssReadingSheetList, false);

        ssReadingViewModel = new SSReadingViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_LESSON_INDEX_EXTRA), binding);
        binding.ssWw.setContextMenuCallback(ssReadingViewModel);
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
        SSReadingListAdapter adapter = (SSReadingListAdapter) binding.ssReadingSheetList.getAdapter();
        adapter.setDays(ssLessonInfo.days);

        Glide.with(this)
                .load(ssLessonInfo.lesson.cover)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        resource.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.OVERLAY);
                        return false;
                    }
                })
                .into(binding.ssAppBar.ssCollapsingToolbarBackdrop);
        adapter.notifyDataSetChanged();
        binding.invalidateAll();
    }

    public static String readFileFromAssets(Context context, String assetPath){

        StringBuilder buf = new StringBuilder();
        try {
            InputStream json = context.getAssets().open(assetPath);
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            return buf.toString();

        } catch (IOException e){ return "";  }
    }

    @Override
    public void onReadChanged(SSRead ssRead){
        binding.ssWw.loadDataWithBaseURL("file:///android_asset/reader/",
                readFileFromAssets(this, "reader/index.html")
                        .replaceAll("\\{\\{content\\}\\}", ssRead.content),
                "text/html", "utf-8", null);

        binding.ssAppBar.ssCollapsingToolbar.setTitle(ssRead.title);
    }
}
