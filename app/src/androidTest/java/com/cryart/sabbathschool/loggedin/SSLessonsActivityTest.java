/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.loggedin;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.SystemClock;
import androidx.test.rule.ActivityTestRule;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.ui.lessons.SSLessonsActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Locale;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;


@RunWith(JUnit4.class)
public class SSLessonsActivityTest {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<SSLessonsActivity> activityRule = new ActivityTestRule<>(SSLessonsActivity.class, false, false);


    @Before
    public void loginAsAnonymous(){
        FirebaseAuth ssFirebaseAuth = FirebaseAuth.getInstance();

        if (ssFirebaseAuth.getCurrentUser() == null){
            ssFirebaseAuth.signInAnonymously();
        }
    }

    @TargetApi(27)
    @Test
    public void takeScreenshot() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
        Intent grouchyIntent = new Intent();
        // intent stuff

        String lang = Locale.getDefault().getLanguage();

        if (lang.equals("iw")) {
            lang = "he";
        }

        if (lang.equals("fil")) {
            lang = "tl";
        }

        grouchyIntent.putExtra(SSConstants.SS_QUARTERLY_INDEX_EXTRA, lang+"-2019-01");
        activityRule.launchActivity(grouchyIntent);

        SystemClock.sleep(4000);
        //Screengrab.screenshot("lessons_screen");
    }
}
