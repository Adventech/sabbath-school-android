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
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSUserManager;
import com.cryart.sabbathschool.model.SSUser;
import com.cryart.sabbathschool.view.SSLoginActivity;
import com.cryart.sabbathschool.view.SSQuarterliesActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;

public class SSLoginViewModel implements SSViewModel, FirebaseAuth.AuthStateListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = SSLoginViewModel.class.getSimpleName();
    private Context context;
    private FirebaseAuth ssFirebase;

    private CallbackManager ssFacebookCallbackManager;
    private GoogleApiClient ssGoogleApiClient;

    public ObservableInt ssLoginLoadingVisibility;
    public ObservableInt ssLoginControlsVisibility;

    // Currently firebase has bug. This intends to prevent onAuthStateChanged()
    // to be called twice
    private boolean firebaseBugFlag = true;

    public SSLoginViewModel(Context context) {
        this.context = context;

        this.configureGoogleLogin();
        this.configureFacebookLogin();
        this.configureFirebase();

        this.ssLoginLoadingVisibility = new ObservableInt(View.INVISIBLE);
        this.ssLoginControlsVisibility = new ObservableInt(View.VISIBLE);
    }

    private void configureFirebase(){
        ssFirebase = FirebaseAuth.getInstance();
        ssFirebase.addAuthStateListener(this);
    }

    private void configureGoogleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build();
        ssGoogleApiClient = new GoogleApiClient.Builder(context).enableAutoManage((SSLoginActivity)context, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void configureFacebookLogin(){
        FacebookSdk.sdkInitialize(context.getApplicationContext());

        ssFacebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(ssFacebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // TODO: Handle unsuccessful / cancel
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        loginFailed(exception.getMessage());
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        loginFailed(connectionResult.getErrorMessage());
    }

    private void handleGoogleAccessToken(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        ssFirebase.signInWithCredential(credential)
                .addOnCompleteListener((SSLoginActivity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        ssFirebase.signInWithCredential(credential)
                .addOnCompleteListener((SSLoginActivity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    private void openApp(){
        Intent launchNextActivity;
        launchNextActivity = new Intent(context, SSQuarterliesActivity.class);
        launchNextActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(launchNextActivity);
        ((SSLoginActivity)context).finish();
    }

    private void loginFailed(String message){
        Crashlytics.log(message);
        Toast.makeText(context, "Login failed. Please try again", Toast.LENGTH_SHORT).show();
        this.ssLoginLoadingVisibility.set(View.INVISIBLE);
        this.ssLoginControlsVisibility.set(View.VISIBLE);
    }

    public void processActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == SSConstants.SS_GOOGLE_SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                handleGoogleAccessToken(acct);
            } else {
                loginFailed("Google failed:" + result.getStatus().getStatusMessage());
            }
        } else {
            ssFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && firebaseBugFlag) {
            firebaseBugFlag = false;
            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();
                if (providerId.equals("firebase")) {
                    if (user.isAnonymous()) {
                        SSUserManager.getInstance().setUser(null);
                    } else {
                        String name = profile.getDisplayName();
                        String email = profile.getEmail();
                        Uri photoUrl = profile.getPhotoUrl();
                        String photo = photoUrl != null ? photoUrl.toString() : "";
                        SSUserManager.getInstance().setUser(new SSUser(name, email, photo));
                    }
                    openApp();
                }
            }
        }
    }

    public void initGoogleLogin(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(ssGoogleApiClient);
        ((SSLoginActivity)context).startActivityForResult(signInIntent, SSConstants.SS_GOOGLE_SIGN_IN_CODE);
    }

    public void initFacebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions((SSLoginActivity)context, Arrays.asList("public_profile", "email"));
    }

    public void initAnonymousLogin(){
        ssFirebase.signInAnonymously()
                .addOnCompleteListener((SSLoginActivity)context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginFailed(task.getException().getMessage());
                        }
                    }
                });

    }

    public void onClickSignIn(View view){
        this.ssLoginLoadingVisibility.set(View.VISIBLE);
        this.ssLoginControlsVisibility.set(View.INVISIBLE);
        initGoogleLogin();
    }

    public void onClickSignInFB(View view){
        this.ssLoginLoadingVisibility.set(View.VISIBLE);
        this.ssLoginControlsVisibility.set(View.INVISIBLE);
        initFacebookLogin();
    }

    public void onClickSignInAnonymous(View view){
        this.ssLoginLoadingVisibility.set(View.VISIBLE);
        this.ssLoginControlsVisibility.set(View.INVISIBLE);
        initAnonymousLogin();
    }

    @Override
    public void destroy() {
        this.context = null;
        ssFirebase.removeAuthStateListener(this);
        ssFirebase = null;
        ssFacebookCallbackManager = null;
        ssGoogleApiClient = null;
    }
}
