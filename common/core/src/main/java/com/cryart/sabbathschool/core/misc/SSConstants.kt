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
package com.cryart.sabbathschool.core.misc

object SSConstants {
    const val SS_APP_RATE_INSTALL_DAYS = 7
    const val SS_DATE_FORMAT = "dd/MM/yyyy"
    const val SS_DATE_FORMAT_OUTPUT = "d MMMM"
    const val SS_DATE_FORMAT_OUTPUT_DAY = "EEEE. d MMMM"
    private const val SS_FIREBASE_API_PREFIX = "/api/v1"
    const val SS_FIREBASE_LANGUAGES_DATABASE = "$SS_FIREBASE_API_PREFIX/languages"
    const val SS_FIREBASE_QUARTERLIES_DATABASE = "$SS_FIREBASE_API_PREFIX/quarterlies"
    const val SS_FIREBASE_QUARTERLY_INFO_DATABASE = "$SS_FIREBASE_API_PREFIX/quarterly-info"
    const val SS_FIREBASE_LESSON_INFO_DATABASE = "$SS_FIREBASE_API_PREFIX/lesson-info"
    const val SS_FIREBASE_READS_DATABASE = "$SS_FIREBASE_API_PREFIX/reads"
    const val SS_FIREBASE_HIGHLIGHTS_DATABASE = "highlights"
    const val SS_FIREBASE_COMMENTS_DATABASE = "comments"
    const val SS_FIREBASE_SUGGESTIONS_DATABASE = "suggestions"
    const val SS_QUARTERLY_INDEX_EXTRA = "SS_QUARTERLY_INDEX"
    const val SS_LESSON_INDEX_EXTRA = "SS_LESSON_INDEX"
    const val SS_READ_INDEX_EXTRA = "SS_READ_INDEX"
    const val SS_READ_VERSE_EXTRA = "SS_READ_VERSE_EXTRA"
    const val SS_COLOR_THEME_LAST_PRIMARY = "SS_COLOR_THEME_LAST_PRIMARY"
    const val SS_COLOR_THEME_LAST_PRIMARY_DARK = "SS_COLOR_THEME_LAST_PRIMARY_DARK"
    const val SS_REMINDER_JOB_ID = "ss_reminder_job_id"
    const val SS_REMINDER_TIME_SETTINGS_FORMAT = "HH:mm"
    const val SS_SETTINGS_REMINDER_ENABLED_KEY = "ss_settings_reminder_enabled"
    const val SS_SETTINGS_REMINDER_ENABLED_DEFAULT_VALUE = true
    const val SS_SETTINGS_REMINDER_TIME_KEY = "ss_settings_reminder_time"
    const val SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE = "08:00"
    const val SS_SETTINGS_THEME_KEY = "ss_settings_display_options_theme"
    const val SS_SETTINGS_FONT_KEY = "ss_settings_display_options_font"
    const val SS_SETTINGS_SIZE_KEY = "ss_settings_display_options_size"
    const val SS_LAST_LANGUAGE_INDEX = "ss_last_language_index"
    const val SS_LAST_QUARTERLY_INDEX = "ss_last_quarterly_index"
    const val SS_LAST_QUARTERLY_TYPE = "ss_last_quarterly_type"
    const val SS_LAST_BIBLE_VERSION_USED = "ss_last_bible_version_used"
    const val SS_LANGUAGE_FILTER_PROMPT_SEEN = "ss_language_filter_prompt_seen"
    const val SS_READER_APP_BASE_URL = "file:///android_asset/reader/"
    const val SS_READER_APP_ENTRYPOINT = "reader/index.html"
    const val SS_EVENT_APP_OPEN = "ss_app_open"
    const val SS_EVENT_LANGUAGE_FILTER = "ss_language_filter"
    const val SS_EVENT_READ_OPEN = "ss_read_open"
    const val SS_EVENT_HIGHLIGHTS_OPEN = "ss_highlights_open"
    const val SS_EVENT_NOTES_OPEN = "ss_notes_open"
    const val SS_EVENT_ABOUT_OPEN = "ss_about_open"
    const val SS_EVENT_BIBLE_OPEN = "ss_bible_open"
    const val SS_EVENT_SETTINGS_OPEN = "ss_settings_open"
    const val SS_EVENT_READ_OPTIONS_OPEN = "ss_read_options_open"
    const val SS_EVENT_TEXT_HIGHLIGHTED = "ss_text_highlighted"
    const val SS_EVENT_COMMENT_CREATED = "ss_comment_created"
    const val SS_EVENT_PARAM_USER_ID = "ss_user_id"
    const val SS_EVENT_PARAM_USER_NAME = "ss_user_name"
    const val SS_EVENT_PARAM_LESSON_INDEX = "ss_lesson_index"
    const val SS_EVENT_PARAM_READ_INDEX = "ss_read_index"
    const val SS_READER_ARTIFACT_NAME = "sabbath-school-reader-latest.zip"
    const val SS_READER_ARTIFACT_CREATION_TIME = "sabbath-school-reader-creation-time"
    const val SS_APP_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cryart.sabbathschool"
}
