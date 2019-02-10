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

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.adapter.SSReadingSheetAdapter;
import com.cryart.sabbathschool.adapter.SSReadingViewAdapter;
import com.cryart.sabbathschool.databinding.SsReadingActivityBinding;
import com.cryart.sabbathschool.misc.SSColorTheme;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSUnzip;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.model.SSReadComments;
import com.cryart.sabbathschool.model.SSReadHighlights;
import com.cryart.sabbathschool.viewmodel.SSReadingViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

public class SSReadingActivity extends SSBaseActivity implements SSReadingViewModel.DataListener, ViewPager.OnPageChangeListener {
    private static final String TAG = SSReadingActivity.class.getSimpleName();

    public SsReadingActivityBinding binding;
    public SSReadingViewModel ssReadingViewModel;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference latestReaderArtifactRef = storageRef.child(SSConstants.SS_READER_ARTIFACT_NAME);
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkIfReaderNeeded();

        binding = DataBindingUtil.setContentView(this, R.layout.ss_reading_activity);

        SSReadingSheetAdapter adapter = new SSReadingSheetAdapter();
        binding.ssReadingSheetList.setAdapter(adapter);
        binding.ssReadingSheetList.setLayoutManager(new LinearLayoutManager(this));

        setSupportActionBar(binding.ssReadingAppBar.ssReadingToolbar);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }

        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setCollapsedTitleTextAppearance(R.style.AppThemeAppBarTextStyle);
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setExpandedTitleTextAppearance(R.style.AppThemeAppBarTextStyleExpanded);

        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setCollapsedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Bold.ttf"));
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setExpandedTitleTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Bold.ttf"));

        ViewCompat.setNestedScrollingEnabled(binding.ssReadingSheetList, false);

        ssReadingViewModel = new SSReadingViewModel(this, this, getIntent().getExtras().getString(SSConstants.SS_LESSON_INDEX_EXTRA), getIntent().getExtras().getString(SSConstants.SS_READ_INDEX_EXTRA), binding);
        ((SSReadingSheetAdapter)binding.ssReadingSheetList.getAdapter()).setReadingViewModel(ssReadingViewModel);

        binding.ssReadingViewPager.setAdapter(new SSReadingViewAdapter(this, ssReadingViewModel));
        binding.ssReadingViewPager.addOnPageChangeListener(this);

        binding.executePendingBindings();
        binding.setViewModel(ssReadingViewModel);

        setUpDrawer();
        updateColorScheme();
    }

    private void checkIfReaderNeeded(){
        latestReaderArtifactRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long lastReaderArtifactCreationTime = prefs.getLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, 0);

                if (lastReaderArtifactCreationTime != storageMetadata.getCreationTimeMillis()){
                    downloadLatestReader(storageMetadata.getUpdatedTimeMillis());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

    private void downloadLatestReader(final long readerArtifactCreationTime){
        final File localFile = new File(getFilesDir(), SSConstants.SS_READER_ARTIFACT_NAME);

        latestReaderArtifactRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(SSConstants.SS_READER_ARTIFACT_CREATION_TIME, readerArtifactCreationTime);
                editor.commit();

                new SSUnzip(localFile.getPath(), getFilesDir().getPath() + "/");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void updateColorScheme(){
        int primaryColor = Color.parseColor(SSColorTheme.getInstance().getColorPrimary());
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setContentScrimColor(primaryColor);
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setBackgroundColor(primaryColor);
        binding.ssReadingSheetHeader.setBackgroundColor(primaryColor);

        updateWindowColorScheme(false);
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

        if (id == R.id.ss_reading_menu_share){
            shareApp((String) getTitle());
        } else if (id == R.id.ss_reading_menu_suggest_edit){
            ssReadingViewModel.promptForEditSuggestion();
        } else if (id == R.id.ss_reading_menu_display_options){
            ssReadingViewModel.onDisplayOptionsClick();
        } else if (id == R.id.ss_reading_menu_settings){
            onSettingsClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        ssReadingViewModel.destroy();
        try {
            ((BitmapDrawable) binding.ssReadingAppBar.ssCollapsingToolbarBackdrop.getDrawable()).getBitmap().recycle();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        super.onDestroy();
    }

    @Override
    public void onLogoutEvent(){
        finish();
    }

    @Override
    public void onLessonInfoChanged(SSLessonInfo ssLessonInfo){
        final SSReadingSheetAdapter adapter = (SSReadingSheetAdapter) binding.ssReadingSheetList.getAdapter();
        adapter.setDays(ssLessonInfo.days);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(ssLessonInfo.lesson.cover,
                binding.ssReadingAppBar.ssCollapsingToolbarBackdrop,
                new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .cacheOnDisk(true)
                        .cacheInMemory(true)
                        .build());

        binding.invalidateAll();
        adapter.notifyDataSetChanged();
    }

    private void setPageTitleAndSubtitle(String title, String subTitle){
        binding.ssReadingAppBar.ssReadingCollapsingToolbar.setTitle(title);
        binding.ssReadingAppBar.ssCollapsingToolbarSubtitle.setText(subTitle);
    }

    @Override
    public void onReadsDownloaded(List<SSRead> ssReads, List<SSReadHighlights> ssReadHighlights, List<SSReadComments> ssReadComments, int ssReadIndex){
        SSReadingViewAdapter ssReadingViewAdapter = (SSReadingViewAdapter) binding.ssReadingViewPager.getAdapter();
        ssReadingViewAdapter.setSSReads(ssReads);
        ssReadingViewAdapter.setSSReadComments(ssReadComments);
        ssReadingViewAdapter.setSSReadHighlights(ssReadHighlights);
        ssReadingViewAdapter.notifyDataSetChanged();
        binding.ssReadingViewPager.setCurrentItem(ssReadIndex);
        setPageTitleAndSubtitle(ssReads.get(ssReadIndex).title, ssReadingViewModel.formatDate(ssReads.get(ssReadIndex).date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY));
    }

    @Override
    public void onBackPressed() {
        if (binding.ssReadingSheet.getVisibility() == View.VISIBLE){
            binding.getViewModel().onMenuClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        SSReadingViewAdapter ssReadingViewAdapter = (SSReadingViewAdapter) binding.ssReadingViewPager.getAdapter();
        SSRead ssRead = ssReadingViewAdapter.ssReads.get(position);
        setPageTitleAndSubtitle(ssRead.title, ssReadingViewModel.formatDate(ssRead.date, SSConstants.SS_DATE_FORMAT_OUTPUT_DAY));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
