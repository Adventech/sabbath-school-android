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

package com.cryart.sabbathschool.core.misc;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import timber.log.Timber;

public class SSColorTheme {
    private final static String COLOR_PRIMARY_FALLBACK = "#385bb2";
    private final static String COLOR_PRIMARY_DARK_FALLBACK = "#27407d";

    private static final Object lock = new Object();
    private static SSColorTheme INSTANCE;

    private String colorPrimary;
    private String colorPrimaryDark;

    private SSColorTheme(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            this.colorPrimary = sharedPreferences.getString(SSConstants.SS_COLOR_THEME_LAST_PRIMARY, null);
            this.colorPrimaryDark = sharedPreferences.getString(SSConstants.SS_COLOR_THEME_LAST_PRIMARY_DARK, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static SSColorTheme getInstance(Context context) {
        synchronized (lock) {
            if (INSTANCE == null) {
                INSTANCE = new SSColorTheme(context.getApplicationContext());
            }
            return INSTANCE;
        }
    }

    public String getColorPrimary() {
        return (this.colorPrimary != null) ? this.colorPrimary : COLOR_PRIMARY_FALLBACK;
    }

    public String getColorPrimaryDark() {
        return (this.colorPrimaryDark != null) ? this.colorPrimaryDark : COLOR_PRIMARY_DARK_FALLBACK;
    }
}
