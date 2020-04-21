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

package com.cryart.sabbathschool.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSEvent;
import com.cryart.sabbathschool.ui.quarterlies.QuarterliesActivity;
import com.cryart.sabbathschool.view.SSLoginActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import java.util.Arrays;
import timber.log.Timber;

public class SSLoginViewModel implements SSViewModel, FirebaseAuth.AuthStateListener {
    private static final String FIREBASE_PROVIDER_ID = "firebase";

    private Context context;
    private FirebaseAuth ssFirebaseAuth;
    private CallbackManager ssFacebookCallbackManager;
    private GoogleSignInClient googleSignInClient;

    public ObservableInt ssLoginLoadingVisibility;
    public ObservableInt ssLoginControlsVisibility;

    // Currently firebase has bug. This intends to prevent onAuthStateChanged()
    // to be called twice
    private boolean firebaseBugFlag = true;

    public SSLoginViewModel(Context context) {
        this.context = context;
        this.ssLoginLoadingVisibility = new ObservableInt(View.INVISIBLE);
        this.ssLoginControlsVisibility = new ObservableInt(View.VISIBLE);

        this.configureGoogleLogin();
        this.configureFacebookLogin();
        this.configureFirebase();
    }

    private void configureFirebase() {
        ssFirebaseAuth = FirebaseAuth.getInstance();
        ssFirebaseAuth.addAuthStateListener(this);
    }

    private void configureGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    private void configureFacebookLogin() {
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        LoginManager.getInstance().logOut();
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

    private void handleGoogleAccessToken(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        ssFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((SSLoginActivity) context, task -> {
                    if (!task.isSuccessful()) {
                        loginFailed(task.getException().getMessage());
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        ssFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((SSLoginActivity) context, task -> {
                    if (!task.isSuccessful()) {
                        loginFailed(task.getException().getMessage());
                    }
                });
    }

    private void openApp() {
        Intent launchNextActivity;
        launchNextActivity = new Intent(context, QuarterliesActivity.class);
        launchNextActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(launchNextActivity);
        ((SSLoginActivity) context).finish();
    }

    private void loginFailed(String message) {
        Timber.e(message);
        Toast.makeText(context, context.getString(R.string.ss_login_failed), Toast.LENGTH_SHORT).show();
        this.ssLoginLoadingVisibility.set(View.INVISIBLE);
        this.ssLoginControlsVisibility.set(View.VISIBLE);
    }

    public void processActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SSConstants.SS_GOOGLE_SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        handleGoogleAccessToken(account);
                    }
                } catch (ApiException e) {
                    Timber.e(e);
                    loginFailed(e.getMessage());
                }

            } else {
                String message = "";
                if (task.getException() != null) {
                    message = task.getException().getMessage();
                }
                loginFailed(message);
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
                if (providerId.equals(FIREBASE_PROVIDER_ID)) {
                    if (!user.isAnonymous()) {
                        if (context != null) {
                            String name = profile.getDisplayName();
                            String email = profile.getEmail();
                            Uri photoUrl = profile.getPhotoUrl();
                            String photo = photoUrl != null ? photoUrl.toString() : "";

                            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString(SSConstants.SS_USER_NAME_INDEX, name);
                            editor.putString(SSConstants.SS_USER_EMAIL_INDEX, email);
                            editor.putString(SSConstants.SS_USER_PHOTO_INDEX, photo);
                            editor.apply();
                        }
                    }

                    if (context != null) {
                        SSEvent.track(context, SSConstants.SS_EVENT_APP_OPEN);
                    }

                    openApp();
                }
            }
        }
    }

    private void initGoogleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        ((SSLoginActivity) context).startActivityForResult(signInIntent, SSConstants.SS_GOOGLE_SIGN_IN_CODE);
    }

    private void initAnonymousLogin() {
        ssFirebaseAuth.signInAnonymously()
                .addOnCompleteListener((SSLoginActivity) context, task -> {
                    if (!task.isSuccessful()) {
                        loginFailed(task.getException().getMessage());
                    }
                });

    }

    public void onClickSignInGoogle() {
        this.ssLoginLoadingVisibility.set(View.VISIBLE);
        this.ssLoginControlsVisibility.set(View.INVISIBLE);
        initGoogleLogin();
    }

    public void onClickSignInFB() {
        LoginManager.getInstance()
                .logInWithReadPermissions((SSLoginActivity) context, Arrays.asList("public_profile", "email"));
    }

    public void onClickSignInAnonymous() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.ss_login_anonymously_dialog_title)
                .setMessage(R.string.ss_login_anonymously_dialog_description)
                .setPositiveButton(context.getString(R.string.ss_login_anonymously_dialog_positive), (dialogInterface, i) -> {
                    ssLoginLoadingVisibility.set(View.VISIBLE);
                    ssLoginControlsVisibility.set(View.INVISIBLE);
                    initAnonymousLogin();
                })
                .setNegativeButton(context.getString(R.string.ss_login_anonymously_dialog_negative), null)
                .create()
                .show();
    }

    @Override
    public void destroy() {
        this.context = null;
        ssFirebaseAuth.removeAuthStateListener(this);
        ssFirebaseAuth = null;
        ssFacebookCallbackManager = null;
    }
}
