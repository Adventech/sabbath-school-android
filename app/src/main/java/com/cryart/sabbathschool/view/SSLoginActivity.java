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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsLoginActivityBinding;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.viewmodel.SSLoginViewModel;
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

import java.util.Arrays;

public class SSLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = SSLoginActivity.class.getSimpleName();
    private FirebaseAuth firebaseRef;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private CallbackManager ssFacebookCallbackManager;
    private GoogleApiClient ssGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.configureGoogleLogin();
        this.configureFacebookLogin();
        this.configureFirebase();

        SsLoginActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.ss_login_activity);
        SSLoginViewModel ssLoginViewModel = new SSLoginViewModel(this);
        binding.setViewModel(ssLoginViewModel);
    }

    private void configureFirebase(){
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    openApp();
                } else {

                }
            }
        };
        firebaseRef = FirebaseAuth.getInstance();
    }

    private void configureGoogleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        ssGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void configureFacebookLogin(){
        FacebookSdk.sdkInitialize(this.getApplicationContext());

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

    public void initGoogleLogin(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(ssGoogleApiClient);
        startActivityForResult(signInIntent, SSConstants.SS_GOOGLE_SIGN_IN_CODE);
    }

    public void initFacebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    public void initAnonymousLogin(){
        firebaseRef.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginFailed(task.getException().getMessage());
                        }
                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        loginFailed(connectionResult.getErrorMessage());
    }

    private void handleGoogleAccessToken(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseRef.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
        firebaseRef.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginFailed(task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRef.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuthStateListener != null) {
            firebaseRef.removeAuthStateListener(firebaseAuthStateListener);
        }
    }

    private void openApp(){
        Intent launchNextActivity;
        launchNextActivity = new Intent(SSLoginActivity.this, SSQuarterliesActivity.class);
        launchNextActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launchNextActivity);
    }

    private void loginFailed(String message){
        throw new RuntimeException(message);
    }
}
