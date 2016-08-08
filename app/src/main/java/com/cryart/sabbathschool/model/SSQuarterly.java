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

public class SSQuarterly {
    public String title;
    public String description;
    public String path;
    public String date;
    public String cover;

    public SSQuarterly() {

    }

    public SSQuarterly(String title, String description, String path, String date, String cover){
        this.title = title;
        this.description = description;
        this.path = path;
        this.date = date;
        this.cover = cover;
    }


//
//    @Override
//    public int describeContents(){
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags){
//        dest.writeString(title);
//        dest.writeString(description);
//        dest.writeString(path);
//        dest.writeString(date);
//        dest.writeString(cover);
//    }
//
//    public static final Creator<SSQuarterly> CREATOR = new Creator<SSQuarterly>() {
//        public SSQuarterly createFromParcel(Parcel source) {
//            return new SSQuarterly(source);
//        }
//
//        public SSQuarterly[] newArray(int size) {
//            return new SSQuarterly[size];
//        }
//    };
}
