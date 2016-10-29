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

package com.cryart.sabbathschool.misc;

public class SSConstants {
    public static final int SS_APP_RATE_INSTALL_DAYS = 7;
    public static final int SS_GOOGLE_SIGN_IN_CODE = 9001;

    public static final String SS_DATE_FORMAT = "dd/MM/yyyy";
    public static final String SS_DATE_FORMAT_OUTPUT = "MMMM d";
    public static final String SS_DATE_FORMAT_OUTPUT_DAY = "EEEE. MMMM d";

    public static final String SS_FIREBASE_API_PREFIX = "/api/v1";
    public static final String SS_FIREBASE_LANGUAGES_DATABASE = SS_FIREBASE_API_PREFIX + "/languages";
    public static final String SS_FIREBASE_QUARTERLIES_DATABASE = SS_FIREBASE_API_PREFIX + "/quarterlies";
    public static final String SS_FIREBASE_QUARTERLY_INFO_DATABASE = SS_FIREBASE_API_PREFIX + "/quarterly-info";
    public static final String SS_FIREBASE_LESSON_INFO_DATABASE = SS_FIREBASE_API_PREFIX + "/lesson-info";
    public static final String SS_FIREBASE_READS_DATABASE = SS_FIREBASE_API_PREFIX + "/reads";

    public static final String SS_FIREBASE_HIGHLIGHTS_DATABASE = "highlights";
    public static final String SS_FIREBASE_COMMENTS_DATABASE = "comments";
    public static final String SS_FIREBASE_SUGGESTIONS_DATABASE = "suggestions";

    public static final String SS_QUARTERLY_INDEX_EXTRA = "SS_QUARTERLY_INDEX";
    public static final String SS_LESSON_INDEX_EXTRA = "SS_LESSON_INDEX";
    public static final String SS_READ_INDEX_EXTRA = "SS_READ_INDEX";
    public static final String SS_READ_VERSE_EXTRA = "SS_READ_VERSE_EXTRA";

    public static final String SS_SETTINGS_THEME_KEY = "ss_settings_display_options_theme";
    public static final String SS_SETTINGS_FONT_KEY = "ss_settings_display_options_font";
    public static final String SS_SETTINGS_SIZE_KEY = "ss_settings_display_options_size";

    public static final String SS_LAST_LANGUAGE_INDEX = "ss_last_language_index";
    public static final String SS_LAST_QUARTERLY_INDEX = "ss_last_quarterly_index";

    public static final String SS_USER_NAME_INDEX = "ss_user_name_index";
    public static final String SS_USER_EMAIL_INDEX = "ss_user_email_index";
    public static final String SS_USER_PHOTO_INDEX = "ss_user_photo_index";

    public static final String SS_LAST_BIBLE_VERSION_USED = "ss_last_bible_version_used";
}
