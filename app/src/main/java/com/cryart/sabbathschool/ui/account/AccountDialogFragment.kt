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

package com.cryart.sabbathschool.ui.account

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.di.ViewModelFactory
import com.cryart.sabbathschool.extensions.arch.getViewModel
import com.cryart.sabbathschool.extensions.glide.GlideApp
import com.cryart.sabbathschool.view.SSAboutActivity
import com.cryart.sabbathschool.view.SSSettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AccountDialogFragment : AppCompatDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: AccountViewModel

    private lateinit var rootView: View

    private val userAvatar: ImageView by lazy { rootView.findViewById<ImageView>(R.id.userAvatar) }
    private val userNameTextView: TextView by lazy { rootView.findViewById<TextView>(R.id.userName) }
    private val userEmailTextView: TextView by lazy { rootView.findViewById<TextView>(R.id.userEmail) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.ss_fragment_account, container, false)

        val window: Window? = dialog?.window
        window?.setGravity(Gravity.TOP or Gravity.START)
        val params: WindowManager.LayoutParams? = window?.attributes
        params?.y = 100
        window?.attributes = params

        return rootView
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        viewModel = getViewModel(this, viewModelFactory)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer {
            GlideApp.with(this)
                    .load(it.photo)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(userAvatar)

            userNameTextView.text = it.displayName ?: getString(R.string.ss_menu_anonymous_name)
            userEmailTextView.text = it.email ?: getString(R.string.ss_menu_anonymous_email)
        })

        if (showsDialog) {
            (requireDialog() as AlertDialog).setView(rootView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.chip_sign_out).setOnClickListener {
            viewModel.logoutClicked()
            requireActivity().finish()
        }
        view.findViewById<View>(R.id.nav_settings).setOnClickListener {
            val intent = Intent(requireContext(), SSSettingsActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        view.findViewById<View>(R.id.nav_share).setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, String.format("%s - %s", getString(R.string.ss_menu_share_app_text), APP_PLAY_STORE_LINK))
                type = "text/plain"
            }
            if (sendIntent.resolveActivity(requireActivity().packageManager!!) != null) {
                startActivity(Intent.createChooser(sendIntent, getString(R.string.ss_menu_share_app)))
            }
            dismiss()
        }
        view.findViewById<View>(R.id.nav_about).setOnClickListener {
            startActivity(Intent(requireContext(), SSAboutActivity::class.java))
            dismiss()
        }
    }

    companion object {
        private const val APP_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=com.cryart.sabbathschool"
    }
}