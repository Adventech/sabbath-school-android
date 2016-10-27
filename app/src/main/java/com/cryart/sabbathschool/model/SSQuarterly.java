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

package com.cryart.sabbathschool.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SSQuarterly {
    public String id;
    public String title;
    public String description;
    public String human_date;
    public String start_date;
    public String end_date;
    public String cover;
    public String index;
    public String path;
    public String full_path;
    public String lang;
    public String color_primary;
    public String color_primary_dark;

    public SSQuarterly() {}

    public SSQuarterly(String id, String title, String description, String human_date, String start_date, String end_date, String cover, String index, String path, String full_path, String lang, String color_primary, String color_primary_dark){
        this.id = id;
        this.title = title;
        this.description = description;
        this.human_date = human_date;
        this.start_date = start_date;
        this.end_date = end_date;
        this.cover = cover;
        this.index = index;
        this.path = path;
        this.full_path = full_path;
        this.lang = lang;
        this.color_primary = color_primary;
        this.color_primary_dark = color_primary_dark;
    }
}

