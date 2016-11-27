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

package com.cryart.sabbathschool.loggedin;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.model.SSBibleVerses;
import com.cryart.sabbathschool.model.SSReadHighlights;
import com.cryart.sabbathschool.view.SSReadingActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(JUnit4.class)
public class SSReadingActivityTest {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<SSReadingActivity> activityRule = new ActivityTestRule<>(SSReadingActivity.class, false, false);


    @Before
    public void loginAsAnonymous(){
        FirebaseAuth ssFirebaseAuth = FirebaseAuth.getInstance();

        if (ssFirebaseAuth.getCurrentUser() == null){
            ssFirebaseAuth.signInAnonymously();
        }
    }

    @TargetApi(23)
    @Test
    public void takeScreenshot() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        Intent grouchyIntent = new Intent();
        // intent stuff

        grouchyIntent.putExtra(SSConstants.SS_LESSON_INDEX_EXTRA, Locale.getDefault().getLanguage()+"-2016-04-06");
        activityRule.launchActivity(grouchyIntent);

        onView(withId(R.id.ss_reading_navigation_next)).perform(click());
        onView(withId(R.id.ss_reading_navigation_next)).perform(click());

        SystemClock.sleep(2000);

        // en-2016-04-06-03
        // "type:textContent|233$387$9$highlight_yellow$|388$548$10$highlight_blue$"
        HashMap<String, String> highlights = new HashMap<String, String>(){{
            put("en", "type:textContent|233$387$9$highlight_yellow$|388$548$10$highlight_blue$");
            put("de", "type:textContent|205$252$1$highlight_yellow$|257$375$2$highlight_green$");
            put("es", "type:textContent|247$319$1$highlight_yellow$|362$434$2$highlight_blue$");
            put("fr", "type:textContent|278$356$1$highlight_yellow$|410$502$2$highlight_green$");
            put("pt", "type:textContent|124$215$1$highlight_yellow$|273$393$2$highlight_green$");
            put("ru", "type:textContent|256$339$1$highlight_yellow$|340$401$2$highlight_green$");
            put("tr", "type:textContent|143$206$1$highlight_green$|280$384$2$highlight_yellow$");
            put("uk", "type:textContent|227$308$1$highlight_yellow$|309$359$2$highlight_green$");
        }};

        activityRule.getActivity().binding.ssReadingView.setReadHighlights(new SSReadHighlights(Locale.getDefault().getLanguage()+"-2016-04-06-03", highlights.get(Locale.getDefault().getLanguage())));


        SystemClock.sleep(2000);
        Screengrab.screenshot("reading_screen");

        onView(withId(R.id.ss_reading_menu_display_options)).perform(click());
        Screengrab.screenshot("reading_screen_display_options");

        SystemClock.sleep(2000);

        ViewActions.pressBack();

        if (!Locale.getDefault().getLanguage().equals("tr") && activityRule.getActivity().ssReadingViewModel.ssRead.bible.size() > 0){
            SSBibleVerses b = activityRule.getActivity().ssReadingViewModel.ssRead.bible.get(0);


            Map.Entry<String,String> entry=b.verses.entrySet().iterator().next();
            String v = entry.getKey();


            activityRule.getActivity().ssReadingViewModel.onVerseClicked(v);
            SystemClock.sleep(2000);
            Screengrab.screenshot("reading_bible_verses");
        }
    }
}
