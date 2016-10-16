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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cryart.sabbathschool.R;
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

public class SSBaseActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener, AccountHeader.OnAccountHeaderListener, FirebaseAuth.AuthStateListener {
    private static final String TAG = SSBaseActivity.class.getSimpleName();
    private FirebaseAuth firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseRef = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart(){
        super.onStart();
        firebaseRef.addAuthStateListener(this);
    }

    private IDrawerItem[] getDrawerItems(){
        IDrawerItem[] ssDrawerItems = new IDrawerItem[7];

        ssDrawerItems[0] = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1).withSelectable(false);
        ssDrawerItems[1] = new PrimaryDrawerItem().withName("My highlights").withIcon(GoogleMaterial.Icon.gmd_border_color).withIdentifier(2).withSelectable(false);
        ssDrawerItems[2] = new PrimaryDrawerItem().withName("My notes").withIcon(GoogleMaterial.Icon.gmd_comment).withIdentifier(3).withSelectable(false);
        ssDrawerItems[3] = new PrimaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(4).withSelectable(false);
        ssDrawerItems[4] = new DividerDrawerItem();
        ssDrawerItems[5] = new PrimaryDrawerItem().withName("Share app").withIdentifier(5).withSelectable(false);
        ssDrawerItems[6] = new PrimaryDrawerItem().withName("About").withIdentifier(6).withSelectable(false);

        return ssDrawerItems;
    }

    private AccountHeader getAccountHeader(){
        return new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.ss_account_header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Vitaliy Lim").withEmail("vitaliy@adventech.io").withIcon("https://pbs.twimg.com/profile_images/666965875350708224/fAXZsqgw_400x400.png").withIdentifier(100),
                        new ProfileSettingDrawerItem().withName("Sign Out").withIdentifier(101)
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
                .withSelectedItem(-1)
                .build();
    }

    protected void setUpDrawer(){
        new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(getAccountHeader())
                .addDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(this)
                .withSelectedItem(-1)
                .build();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null) {
            Log.d(TAG, String.valueOf(drawerItem.getIdentifier()));
            if (drawerItem.getIdentifier() == 1) {

            } else if (drawerItem.getIdentifier() == 2) {

            }
        }

        return false;
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
        if (profile instanceof ProfileSettingDrawerItem && profile.getIdentifier() == 101) {
            firebaseRef.signOut();
        }
        return false;
    }
}
