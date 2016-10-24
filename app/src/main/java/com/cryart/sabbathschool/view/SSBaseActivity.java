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

package com.cryart.sabbathschool.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSUserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class SSBaseActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener, AccountHeader.OnAccountHeaderListener, FirebaseAuth.AuthStateListener {
    private static final String TAG = SSBaseActivity.class.getSimpleName();
    private FirebaseAuth ssFirebase;
    private static final int MENU_READ_ID = 1;
    private static final int MENU_HIGHLIGHTS_ID = 2;
    private static final int MENU_NOTES_ID = 3;
    private static final int MENU_SETTINGS_ID = 4;
    private static final int MENU_SHARE_ID = 5;
    private static final int MENU_ABOUT_ID = 6;

    private static final int MENU_HEADER_LOGOUT_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ssFirebase = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart(){
        super.onStart();
        ssFirebase.addAuthStateListener(this);
    }

    private IDrawerItem[] getDrawerItems(){
        IDrawerItem[] ssDrawerItems = new IDrawerItem[7];

        ssDrawerItems[0] = new PrimaryDrawerItem().withName("Read now").withIcon(GoogleMaterial.Icon.gmd_book).withIdentifier(MENU_READ_ID).withSelectable(false);
        ssDrawerItems[1] = new PrimaryDrawerItem().withName("My Highlights").withIcon(GoogleMaterial.Icon.gmd_border_color).withIdentifier(MENU_HIGHLIGHTS_ID).withSelectable(false);
        ssDrawerItems[2] = new PrimaryDrawerItem().withName("My Notes").withIcon(GoogleMaterial.Icon.gmd_comment).withIdentifier(MENU_NOTES_ID).withSelectable(false);
        ssDrawerItems[3] = new PrimaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(MENU_SETTINGS_ID).withSelectable(false);
        ssDrawerItems[4] = new DividerDrawerItem();
        ssDrawerItems[5] = new PrimaryDrawerItem().withName("Share Sabbath School").withIdentifier(MENU_SHARE_ID).withSelectable(false);
        ssDrawerItems[6] = new PrimaryDrawerItem().withName("About").withIdentifier(MENU_ABOUT_ID).withSelectable(false);

        return ssDrawerItems;
    }

    private AccountHeader getAccountHeader(){
        return new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.color.colorPrimary)
                .addProfiles(
                        new ProfileDrawerItem().withName(SSUserManager.getInstance().user.name).withEmail(SSUserManager.getInstance().user.email).withIcon(SSUserManager.getInstance().user.photo).withIdentifier(100),
                        new ProfileSettingDrawerItem().withName("Sign Out").withIdentifier(MENU_HEADER_LOGOUT_ID)
                )
                .withOnAccountHeaderListener(this)
                .build();
    }


    protected void setUpDrawer(Toolbar ssToolbar){
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(ssToolbar)
                .withAccountHeader(getAccountHeader())
                .addDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(this)
                .withSelectedItem(1)
                .build();
    }

    protected void setUpDrawer(){
        new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(getAccountHeader())
                .addDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(this)
                .withSelectedItem(1)
                .build();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null) {
            if (drawerItem.getIdentifier() == MENU_READ_ID) {

            } else if (drawerItem.getIdentifier() == MENU_HIGHLIGHTS_ID) {
                Intent intent = new Intent(SSBaseActivity.this, SSMyHighlightsActivity.class);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == MENU_NOTES_ID) {
                Intent intent = new Intent(SSBaseActivity.this, SSMyNotesActivity.class);
                startActivity(intent);
            } else if (drawerItem.getIdentifier() == MENU_SETTINGS_ID) {
                onSettingsClick();
            } else if (drawerItem.getIdentifier() == MENU_SHARE_ID) {
                onShareAppClick();
            } else if (drawerItem.getIdentifier() == MENU_ABOUT_ID) {
                startActivity(new Intent(SSBaseActivity.this, SSAboutActivity.class));
            }
        }
        return false;
    }

    private void onShareAppClick(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sabbath School - https://play.google.com/store/apps/details?id=com.cryart.sabbathschool");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void onSettingsClick(){
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

    @Override
    public boolean onProfileChanged(View view, IProfile profile, boolean current){
        if (profile instanceof ProfileSettingDrawerItem && profile.getIdentifier() == MENU_HEADER_LOGOUT_ID) {
            ssFirebase.signOut();
            onLogoutEvent();
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public abstract void onLogoutEvent();
}
