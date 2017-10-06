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
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.adapter.SSQuarterliesAdapter;
import com.cryart.sabbathschool.adapter.SSQuarterliesLanguageFilterAdapter;
import com.cryart.sabbathschool.databinding.SsQuarterliesActivityBinding;
import com.cryart.sabbathschool.misc.SSColorTheme;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSReminder;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.model.SSQuarterlyLanguage;
import com.cryart.sabbathschool.viewmodel.SSQuarterliesViewModel;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class SSQuarterliesActivity extends SSBaseActivity implements SSQuarterliesViewModel.DataListener {
    private static final String TAG = SSQuarterliesActivity.class.getSimpleName();

    private SsQuarterliesActivityBinding binding;
    private SSQuarterliesViewModel ssQuarterliesViewModel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lastQuarterlyIndex = prefs.getString(SSConstants.SS_LAST_QUARTERLY_INDEX, null);

        if (lastQuarterlyIndex != null){
            Intent launchNextActivity;
            launchNextActivity = new Intent(this, SSLessonsActivity.class);
            launchNextActivity.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, lastQuarterlyIndex);
            startActivity(launchNextActivity);
        }

        if (!prefs.contains(SSConstants.SS_SETTINGS_REMINDER_ENABLED_KEY)){
            WakefulIntentService.scheduleAlarms(new SSReminder(), SSApplication.get());
        }

        binding = DataBindingUtil.setContentView(this, R.layout.ss_quarterlies_activity);
        setSupportActionBar(binding.ssAppBar.ssToolbar);

        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayShowCustomEnabled(true);
            ssToolbar.setDisplayShowTitleEnabled(false);
        }

        SSQuarterliesAdapter adapter = new SSQuarterliesAdapter();
        binding.ssQuarterliesList.setAdapter(adapter);
        binding.ssQuarterliesList.setLayoutManager(new LinearLayoutManager(this));

        SSQuarterliesLanguageFilterAdapter languagesAdapter = new SSQuarterliesLanguageFilterAdapter();
        binding.ssLanguageMenu.ssLanguageMenuList.setAdapter(languagesAdapter);
        binding.ssLanguageMenu.ssLanguageMenuList.setLayoutManager(new LinearLayoutManager(this));
        ViewCompat.setNestedScrollingEnabled(binding.ssLanguageMenu.ssLanguageMenuList, false);

        ssQuarterliesViewModel = new SSQuarterliesViewModel(this, this, binding);
        ((SSQuarterliesLanguageFilterAdapter)binding.ssLanguageMenu.ssLanguageMenuList.getAdapter()).setQuarterliesViewModel(ssQuarterliesViewModel);

        binding.executePendingBindings();
        binding.setViewModel(ssQuarterliesViewModel);

        setUpDrawer(binding.ssAppBar.ssToolbar);
    }

    private void updateColorScheme(){
        int primaryColor = Color.parseColor(SSColorTheme.getInstance().getColorPrimary());
        binding.ssAppBar.ssToolbar.setBackgroundColor(primaryColor);
        binding.ssLanguageMenu.ssLanguageMenuHeader.setBackgroundColor(primaryColor);

        updateWindowColorScheme();
    }

    @Override
    public void onQuarterliesChanged(List<SSQuarterly> ssQuarterlies) {
        if (ssQuarterlies.size() > 0) {
            SSColorTheme.getInstance().setColorPrimary(ssQuarterlies.get(0).color_primary);
            SSColorTheme.getInstance().setColorPrimaryDark(ssQuarterlies.get(0).color_primary_dark);
            updateColorScheme();
        }

        SSQuarterliesAdapter adapter = (SSQuarterliesAdapter) binding.ssQuarterliesList.getAdapter();
        adapter.setQuarterlies(ssQuarterlies);
        adapter.notifyDataSetChanged();

        boolean languageFilterPromptSeen = prefs.getBoolean(SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, false);

        if (!languageFilterPromptSeen) {
            new MaterialTapTargetPrompt.Builder(this)
                    .setTarget(R.id.ss_quarterlies_menu_filter)
                    .setPrimaryText(getString(R.string.ss_quarterlies_filter_languages_prompt_title))
                    .setIconDrawableColourFilter(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()))
                    .setIconDrawable(new IconicsDrawable(this)
                            .icon(GoogleMaterial.Icon.gmd_filter_list)
                            .color(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()))
                            .sizeDp(18))
                    .setBackgroundColour(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()))
                    .setSecondaryText(R.string.ss_quarterlies_filter_languages_prompt_description)
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener(){
                        @Override
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state){
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED){
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean(SSConstants.SS_LANGUAGE_FILTER_PROMPT_SEEN, true);
                                editor.commit();
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onQuarterliesLanguagesChanged(List<SSQuarterlyLanguage> quarterlyLanguages){
        SSQuarterliesLanguageFilterAdapter adapter = (SSQuarterliesLanguageFilterAdapter) binding.ssLanguageMenu.ssLanguageMenuList.getAdapter();
        adapter.setQuarterlyLanguages(quarterlyLanguages);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ssQuarterliesViewModel.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ss_quarterlies_menu, menu);
        menu.getItem(0).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_translate)
                .color(Color.WHITE)
                .sizeDp(18));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ss_quarterlies_menu_filter) {
            ssQuarterliesViewModel.onMenuClick();
            return true;
        } else if (id == R.id.ss_quarterlies_menu_share) {
            shareApp(getString(R.string.ss_menu_share_app_text));
        } else if (id == R.id.ss_quarterlies_menu_settings) {
            onSettingsClick();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLogoutEvent(){
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (binding.ssLanguageMenu.ssLanguageMenu.getVisibility() == View.VISIBLE){
            ssQuarterliesViewModel.onMenuClick();
        } else {
            super.onBackPressed();
        }
    }
}
