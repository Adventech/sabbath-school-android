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

package com.cryart.sabbathschool.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.extensions.glide.GlideApp;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.ui.account.AccountDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class SSBaseActivity extends SSColorSchemeActivity implements FirebaseAuth.AuthStateListener {

    private static final String APP_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cryart.sabbathschool";
    public static final String MENU_ANONYMOUS_PHOTO = "https://sabbath-school.adventech.io/api/v1/anonymous-photo.png";

    private FirebaseAuth ssFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ssFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ssFirebaseAuth.addAuthStateListener(this);
    }

    protected void setupAccountToolbar(final Toolbar ssToolbar) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String photo = prefs.getString(SSConstants.SS_USER_PHOTO_INDEX, null);

        int size = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        GlideApp.with(this)
                .asDrawable()
                .load((photo == null) ? R.drawable.ic_account_circle_white : photo)
                .placeholder(R.drawable.ic_account_circle_white)
                .error(R.drawable.ic_account_circle_white)
                .circleCrop()
                .into(new SimpleTarget<Drawable>(size, size) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ssToolbar.setNavigationIcon(resource);
                    }
                });


        ssToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountDialogFragment fragment = new AccountDialogFragment();
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });
    }

    public void updateWindowColorScheme() {
        updateWindowColorScheme(true);
    }

    private void onShareAppClick() {
        shareApp(getString(R.string.ss_menu_share_app_text));
    }

    public void shareApp(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s - %s", message, APP_PLAY_STORE_LINK));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void onSettingsClick() {
        Intent intent = new Intent(SSBaseActivity.this, SSSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Intent ssLoginActivityIntent = new Intent(SSBaseActivity.this, SSLoginActivity.class);
            startActivity(ssLoginActivityIntent);
        }
    }
}
