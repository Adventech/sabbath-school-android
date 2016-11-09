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


public class SSColorTheme {
    private final static String COLOR_PRIMARY_FALLBACK = "#16a365";
    private final static String COLOR_PRIMARY_DARK_FALLBACK = "#18704A";
    private String colorPrimary;
    private String colorPrimaryDark;


    private static class Holder {
        static final SSColorTheme INSTANCE = new SSColorTheme();
    }

    public static SSColorTheme getInstance() {
        return Holder.INSTANCE;
    }

    public String getColorPrimary(){
        return (this.colorPrimary != null) ? this.colorPrimary : COLOR_PRIMARY_FALLBACK;
    }

    public String getColorPrimaryDark(){
        return (this.colorPrimaryDark != null) ? this.colorPrimaryDark : COLOR_PRIMARY_DARK_FALLBACK;
    }

    public void setColorPrimary(String colorPrimary){
        this.colorPrimary = colorPrimary;
    }

    public void setColorPrimaryDark(String colorPrimaryDark){
        this.colorPrimaryDark = colorPrimaryDark;
    }
}