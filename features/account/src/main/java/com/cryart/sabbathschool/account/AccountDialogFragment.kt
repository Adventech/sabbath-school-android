/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.account

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.ss.models.config.AppConfig
import coil.load
import coil.transform.CircleCropTransformation
import com.cryart.sabbathschool.account.databinding.SsFragmentAccountBinding
import com.cryart.sabbathschool.core.extensions.coroutines.flow.collectIn
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ss.misc.SSConstants
import javax.inject.Inject
import app.ss.translations.R as L10n

@AndroidEntryPoint
class AccountDialogFragment : AppCompatDialogFragment() {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appConfig: AppConfig

    private val viewModel: AccountViewModel by viewModels()

    private var binding: SsFragmentAccountBinding? = null

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(appConfig.webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setGravity(Gravity.TOP or Gravity.CENTER)
        binding = SsFragmentAccountBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(binding?.root)
        }

        viewModel.userInfoFlow.collectIn(viewLifecycleOwner) { userInfo ->
            binding?.apply {
                userInfo.photo?.let {
                    userAvatar.load(it) {
                        placeholder(R.drawable.ic_account_circle)
                        error(R.drawable.ic_account_circle)
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
                userName.text = userInfo.displayName ?: getString(L10n.string.ss_menu_anonymous_name)
                userEmail.text = userInfo.email ?: getString(L10n.string.ss_menu_anonymous_email)
            }
        }

        binding?.apply {
            chipSignOut.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    googleSignInClient.signOut().await()
                }
                viewModel.logoutClicked()
                appNavigator.navigate(requireActivity(), Destination.LOGIN)
            }

            navSettings.setOnClickListener {
                appNavigator.navigate(requireActivity(), Destination.SETTINGS)
                dismiss()
            }

            navShare.setOnClickListener {
                val shareIntent = ShareCompat.IntentBuilder(requireContext())
                    .setType("text/plain")
                    .setText(getString(L10n.string.ss_menu_share_app_text, SSConstants.SS_APP_PLAY_STORE_LINK))
                    .intent
                if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(L10n.string.ss_menu_share_app)))
                }

                dismiss()
            }

            navAbout.setOnClickListener {
                appNavigator.navigate(requireActivity(), Destination.ABOUT)
                dismiss()
            }
        }
    }
}
