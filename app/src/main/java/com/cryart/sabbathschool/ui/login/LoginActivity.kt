/*
 * Copyright (c) 2020. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import app.ss.models.config.AppConfig
import app.ss.runtime.permissions.RuntimePermissions
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.extensions.sdk.isBelowApi
import com.cryart.sabbathschool.core.model.ViewState
import com.cryart.sabbathschool.databinding.SsLoginActivityBinding
import com.cryart.sabbathschool.databinding.SsLoginButtonsBinding
import com.cryart.sabbathschool.lessons.ui.quarterlies.QuarterliesActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import app.ss.translations.R as L10n

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var appConfig: AppConfig

    @Inject
    lateinit var runtimePermissions: RuntimePermissions

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var buttonsBinding: SsLoginButtonsBinding

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(appConfig.webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private val getGoogleSignInLauncher: ActivityResultLauncher<GoogleSignInClient> =
        registerForActivityResult(GetSignInDataContract()) { data ->
            viewModel.handleGoogleSignInResult(data)
        }

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SsLoginActivityBinding.inflate(layoutInflater)
        buttonsBinding = SsLoginButtonsBinding.bind(binding.root)
        setContentView(binding.root)

        initUi()

        viewModel.viewStateFlow.collectIn(this) { state ->
            when (state) {
                is ViewState.Success<*> -> launchMain()
                ViewState.Loading -> {
                    buttonsBinding.apply {
                        progressBar.isVisible = true
                        container.isVisible = false
                    }
                }
                is ViewState.Error -> {
                    buttonsBinding.apply {
                        progressBar.isVisible = false
                        container.isVisible = true
                    }

                    val message = state.message ?: state.messageRes?.let {
                        getString(it)
                    } ?: return@collectIn
                    Snackbar.make(buttonsBinding.root, message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok) {}
                        .show()
                }
                null -> {}
            }
        }

        reportFullyDrawn()
        checkNotificationsPermission()
    }

    private fun initUi() {
        buttonsBinding.apply {
            google.setOnClickListener {
                getGoogleSignInLauncher.launch(googleSignInClient)
            }
            anonymous.setOnClickListener {
                MaterialAlertDialogBuilder(this@LoginActivity)
                    .setTitle(L10n.string.ss_login_anonymously_dialog_title)
                    .setMessage(L10n.string.ss_login_anonymously_dialog_description)
                    .setPositiveButton(L10n.string.ss_login_anonymously_dialog_positive) { _: DialogInterface?, _: Int ->
                        viewModel.handleAnonymousLogin()
                    }
                    .setNegativeButton(L10n.string.ss_login_anonymously_dialog_negative, null)
                    .create()
                    .show()
            }

            tvTerms.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun checkNotificationsPermission() {
        if (isBelowApi(Build.VERSION_CODES.TIRAMISU)) {
            return
        }

        with(runtimePermissions) {
            if (!isGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                setup(
                    this@LoginActivity,
                    object : RuntimePermissions.Listener {
                        override fun onPermissionGranted() {
                            viewModel.handleNotificationsPermissionGranted()
                        }

                        override fun onPermissionDenied() {
                            viewModel.handleNotificationsPermissionDenied()
                        }
                    }
                )

                request(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun launchMain() {
        val intent = QuarterliesActivity.launchIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }
}
