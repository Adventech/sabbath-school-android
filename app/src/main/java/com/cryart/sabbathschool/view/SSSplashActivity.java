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

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import static com.cryart.sabbathschool.misc.SSConstants.SS_LESSON_INDEX_EXTRA;
import static com.cryart.sabbathschool.misc.SSConstants.SS_READ_INDEX_EXTRA;

public class SSSplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(SS_LESSON_INDEX_EXTRA)) {
                String ssLessonIndex = getIntent().getExtras().getString(SS_LESSON_INDEX_EXTRA);
                String ssReadIndex = null;
                if (getIntent().getExtras().containsKey(SS_READ_INDEX_EXTRA)){
                    ssReadIndex = getIntent().getExtras().getString(SS_READ_INDEX_EXTRA);
                }

                Intent ssReadingIntent = new Intent(this, SSReadingActivity.class);
                ssReadingIntent.putExtra(SS_LESSON_INDEX_EXTRA, ssLessonIndex);
                ssReadingIntent.putExtra(SS_READ_INDEX_EXTRA, ssReadIndex);
                startActivity(ssReadingIntent);
                finish();
            } else {
                launchLoginActivity();
            }
        } else {
            launchLoginActivity();
        }

        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(1);
        } catch (Exception e){}
    }

    private void launchLoginActivity(){
        Intent intent = new Intent(this, SSLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
