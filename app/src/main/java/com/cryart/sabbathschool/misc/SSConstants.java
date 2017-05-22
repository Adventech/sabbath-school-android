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
    public static final String SS_DATE_FORMAT_OUTPUT = "d MMMM";
    public static final String SS_DATE_FORMAT_OUTPUT_DAY = "EEEE. d MMMM";

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

    public static final String SS_COLOR_THEME_LAST_PRIMARY = "SS_COLOR_THEME_LAST_PRIMARY";
    public static final String SS_COLOR_THEME_LAST_PRIMARY_DARK = "SS_COLOR_THEME_LAST_PRIMARY_DARK";

    public static final String SS_REMINDER_TIME_SETTINGS_FORMAT = "HH:mm";
    public static final String SS_SETTINGS_REMINDER_ENABLED_KEY = "ss_settings_reminder_enabled";
    public static final boolean SS_SETTINGS_REMINDER_ENABLED_DEFAULT_VALUE = true;

    public static final String SS_SETTINGS_REMINDER_TIME_KEY = "ss_settings_reminder_time";
    public static final String SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE = "08:00";

    public static final String SS_SETTINGS_THEME_KEY = "ss_settings_display_options_theme";
    public static final String SS_SETTINGS_FONT_KEY = "ss_settings_display_options_font";
    public static final String SS_SETTINGS_SIZE_KEY = "ss_settings_display_options_size";

    public static final String SS_LAST_LANGUAGE_INDEX = "ss_last_language_index";
    public static final String SS_LAST_QUARTERLY_INDEX = "ss_last_quarterly_index";

    public static final String SS_USER_NAME_INDEX = "ss_user_name_index";
    public static final String SS_USER_EMAIL_INDEX = "ss_user_email_index";
    public static final String SS_USER_PHOTO_INDEX = "ss_user_photo_index";

    public static final String SS_LAST_BIBLE_VERSION_USED = "ss_last_bible_version_used";
    public static final String SS_LANGUAGE_FILTER_PROMPT_SEEN = "ss_language_filter_prompt_seen";

    public static final String SS_READER_APP_BASE_URL = "file:///android_asset/reader/";
    public static final String SS_READER_APP_ENTRYPOINT = "reader/index.html";

    public static final String SS_EVENT_APP_OPEN = "ss_app_open";
    public static final String SS_EVENT_LANGUAGE_FILTER = "ss_language_filter";
    public static final String SS_EVENT_READ_OPEN = "ss_read_open";
    public static final String SS_EVENT_HIGHLIGHTS_OPEN = "ss_highlights_open";
    public static final String SS_EVENT_NOTES_OPEN = "ss_notes_open";
    public static final String SS_EVENT_ABOUT_OPEN = "ss_about_open";
    public static final String SS_EVENT_BIBLE_OPEN = "ss_bible_open";
    public static final String SS_EVENT_SETTINGS_OPEN = "ss_settings_open";
    public static final String SS_EVENT_READ_OPTIONS_OPEN = "ss_read_options_open";
    public static final String SS_EVENT_TEXT_HIGHLIGHTED = "ss_text_highlighted";
    public static final String SS_EVENT_COMMENT_CREATED = "ss_comment_created";

    public static final String SS_EVENT_PARAM_USER_ID = "ss_user_id";
    public static final String SS_EVENT_PARAM_USER_NAME = "ss_user_name";
    public static final String SS_EVENT_PARAM_LESSON_INDEX = "ss_lesson_index";
    public static final String SS_EVENT_PARAM_READ_INDEX = "ss_read_index";

    public static final String SS_READER_ARTIFACT_NAME = "sabbath-school-reader-latest.zip";
    public static final String SS_READER_ARTIFACT_CREATION_TIME = "sabbath-school-reader-creation-time";
}
