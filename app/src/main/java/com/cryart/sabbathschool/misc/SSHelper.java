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

import android.content.Context;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SSHelper {
    public static int convertDpToPixels(Context context, int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Parses time string and returns parsed hour of day
     * @param time Time string
     * @param parseFormat Format of the time string
     * @return Hour of the day or 0 if error
     */
    public static int parseHourFromString(String time, String parseFormat){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(parseFormat);
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (Exception exception){
            return 0;
        }
    }

    /**
     * Parses time string and returns parsed minute of hour
     * @param time Time string
     * @param parseFormat Format of the time string
     * @return Minute of the hour or 0 if error
     */
    public static int parseMinuteFromString(String time, String parseFormat){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(parseFormat);
            Date date = sdf.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            return calendar.get(Calendar.MINUTE);
        } catch (Exception exception){
            return 0;
        }
    }

    /**
     * Parses time string and returns it formatted with <code>returnFormat</code>
     * @param time Time string
     * @param parseFormat Format of the time string
     * @param returnFormat Desired format
     * @return Reformatted string
     */
    public static String parseTimeAndReturnInFormat(String time, String parseFormat, java.text.DateFormat returnFormat){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(parseFormat);
            Date date = sdf.parse(time);
            return returnFormat.format(date);
        } catch (Exception exception){
            return time;
        }
    }
}
