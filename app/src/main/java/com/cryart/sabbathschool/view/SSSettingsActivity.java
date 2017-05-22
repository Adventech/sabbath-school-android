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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsSettingsActivityBinding;
import com.cryart.sabbathschool.misc.SSColorTheme;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSEvent;
import com.cryart.sabbathschool.misc.SSSettingsFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SSSettingsActivity extends SSColorSchemeActivity {
    private SsSettingsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ss_settings_activity);
        setSupportActionBar(binding.ssAppBar.ssToolbar);
        ActionBar ssToolbar = getSupportActionBar();
        if (ssToolbar != null) {
            ssToolbar.setDisplayShowCustomEnabled(true);
            ssToolbar.setDisplayShowTitleEnabled(false);
            ssToolbar.setDisplayHomeAsUpEnabled(true);
        }
        binding.ssAppBar.toolbarTitle.setText(getString(R.string.ss_settings));
        getFragmentManager().beginTransaction().replace(R.id.ss_settings_frame, new SSSettingsFragment()).commit();

        SSEvent.track(SSConstants.SS_EVENT_SETTINGS_OPEN);

        updateColorScheme();
    }

    private void updateColorScheme(){
        binding.ssAppBar.ssToolbar.setBackgroundColor(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()));
        updateWindowColorScheme();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

