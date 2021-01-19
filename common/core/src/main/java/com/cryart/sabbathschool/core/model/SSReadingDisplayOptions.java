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

package com.cryart.sabbathschool.core.model;

public class SSReadingDisplayOptions {
    public static final String SS_THEME_LIGHT = "light";
    public static final String SS_THEME_SEPIA = "sepia";
    public static final String SS_THEME_DARK = "dark";

    public static final String SS_THEME_LIGHT_RGB = "#ffffff";
    public static final String SS_THEME_SEPIA_RGB = "#fbf0d9";
    public static final String SS_THEME_DARK_RGB = "#212325";

    public static final String SS_SIZE_TINY = "tiny";
    public static final String SS_SIZE_SMALL = "small";
    public static final String SS_SIZE_MEDIUM = "medium";
    public static final String SS_SIZE_LARGE = "large";
    public static final String SS_SIZE_HUGE = "huge";

    public static final String SS_FONT_ANDADA = "andada";
    public static final String SS_FONT_LATO = "lato";
    public static final String SS_FONT_PT_SERIF = "pt-serif";
    public static final String SS_FONT_PT_SANS = "pt-sans";

    public String theme;
    public String size;
    public String font;

    public SSReadingDisplayOptions(){
        this(SS_THEME_LIGHT, SS_SIZE_MEDIUM, SS_FONT_ANDADA);
    }

    public SSReadingDisplayOptions(String theme, String size, String font){
        this.theme = theme;
        this.size = size;
        this.font = font;
    }
}
