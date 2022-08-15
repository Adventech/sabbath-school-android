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

import com.cryart.sabbathschool.core.BuildConfig

object SSConstants {
    const val SS_APP_RATE_INSTALL_DAYS = 7
    const val SS_DATE_FORMAT = "dd/MM/yyyy"
    const val SS_DATE_FORMAT_OUTPUT = "MMMM dd"
    const val SS_DATE_FORMAT_OUTPUT_DAY = "EEEE. MMMM dd"
    const val SS_DATE_FORMAT_OUTPUT_DAY_SHORT = "EEE. MMMM dd"
    const val SS_QUARTERLY_INDEX_EXTRA = "SS_QUARTERLY_INDEX"
    const val SS_QUARTERLY_GROUP = "quarterly_group"
    const val SS_LESSON_INDEX_EXTRA = "SS_LESSON_INDEX"
    const val SS_READ_INDEX_EXTRA = "SS_READ_INDEX"
    const val SS_READ_POSITION_EXTRA = "SS_READ_POSITION"
    const val SS_READ_VERSE_EXTRA = "SS_READ_VERSE_EXTRA"
    const val SS_COLOR_THEME_LAST_PRIMARY = "SS_COLOR_THEME_LAST_PRIMARY"
    const val SS_COLOR_THEME_LAST_PRIMARY_DARK = "SS_COLOR_THEME_LAST_PRIMARY_DARK"
    const val SS_REMINDER_SCHEDULED = "ss_reminder_scheduled"
    const val SS_REMINDER_TIME_SETTINGS_FORMAT = "HH:mm"
    const val SS_SETTINGS_REMINDER_ENABLED_KEY = "ss_settings_reminder_enabled"
    const val SS_SETTINGS_REMINDER_TIME_KEY = "ss_settings_reminder_time"
    const val SS_SETTINGS_REMINDER_TIME_DEFAULT_VALUE = "08:00"
    const val SS_SETTINGS_THEME_KEY = "ss_settings_display_options_theme"
    const val SS_SETTINGS_FONT_KEY = "ss_settings_display_options_font"
    const val SS_SETTINGS_SIZE_KEY = "ss_settings_display_options_size"
    const val SS_LAST_LANGUAGE_INDEX = "ss_last_language_index"
    const val SS_LAST_QUARTERLY_INDEX = "ss_last_quarterly_index"
    const val SS_LAST_QUARTERLY_TYPE = "ss_last_quarterly_type"
    const val SS_APP_RE_BRANDING_PROMPT_SEEN = "ss_app_re_branding_prompt_seen"
    const val SS_READER_APP_BASE_URL = "file:///android_asset/reader/"
    const val SS_READER_APP_ENTRYPOINT = "reader/index.html"
    const val SS_EVENT_LANGUAGE_FILTER = "ss_language_filter"
    const val SS_EVENT_ABOUT_OPEN = "ss_about_open"
    const val SS_EVENT_BIBLE_OPEN = "ss_bible_open"
    const val SS_EVENT_SETTINGS_OPEN = "ss_settings_open"
    const val SS_EVENT_READ_OPTIONS_OPEN = "ss_read_options_open"
    const val SS_EVENT_COMMENT_CREATED = "ss_comment_created"
    const val SS_EVENT_PARAM_READ_INDEX = "ss_read_index"
    const val SS_READER_ARTIFACT_NAME = "sabbath-school-reader-latest.zip"
    const val SS_READER_ARTIFACT_LAST_MODIFIED = "sabbath-school-reader-last-modified"
    const val SS_APP_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cryart.sabbathschool"
    const val SS_LATEST_QUARTERLY = "ss_latest_quarterly"
    private const val SS_API_BASE_URL = "https://sabbath-school.adventech.io/"
    private const val SS_STAGE_API_BASE_URL = "https://sabbath-school-stage.adventech.io/"

    fun apiBaseUrl() = if (BuildConfig.DEBUG) SS_STAGE_API_BASE_URL else SS_API_BASE_URL
}
