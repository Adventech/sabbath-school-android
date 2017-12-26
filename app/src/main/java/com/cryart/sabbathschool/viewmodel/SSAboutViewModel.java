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

package com.cryart.sabbathschool.viewmodel;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.cryart.sabbathschool.R;

public class SSAboutViewModel implements SSViewModel{
    private Context context;

    public SSAboutViewModel(Context context){
        this.context = context;
    }

    private void onLinkClick(String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    public void onFacebookClick(){
        onLinkClick(context.getString(R.string.ss_settings_facebook_url));
    }

    public void onInstagramClick(){
        onLinkClick(context.getString(R.string.ss_settings_instagram_url));
    }

    public void onGitHubClick(){
        onLinkClick(context.getString(R.string.ss_settings_github_url));
    }

    public void onWebsiteClick(){
        onLinkClick(context.getString(R.string.ss_settings_website_url));
    }

    @Override
    public void destroy(){}
}
