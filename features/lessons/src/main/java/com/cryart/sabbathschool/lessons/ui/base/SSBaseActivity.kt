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
package com.cryart.sabbathschool.lessons.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.PixelSize
import coil.transform.CircleCropTransformation
import com.cryart.sabbathschool.core.ui.SSColorSchemeActivity
import com.cryart.sabbathschool.lessons.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener

abstract class SSBaseActivity : SSColorSchemeActivity(), AuthStateListener {

    private var ssFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ssFirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        ssFirebaseAuth?.addAuthStateListener(this)
    }

    protected fun setupAccountToolbar(ssToolbar: Toolbar) {
        val size = resources.getDimensionPixelSize(R.dimen.spacing_large)
        val photo = ssFirebaseAuth?.currentUser?.photoUrl
        photo?.let { url ->
            val request = ImageRequest.Builder(this)
                .size(PixelSize(size, size))
                .transformations(CircleCropTransformation())
                .data(url)
                .target {
                    ssToolbar.navigationIcon = it
                }
                .build()
            imageLoader.enqueue(request)
        } ?: ssToolbar.setNavigationIcon(R.drawable.ic_account_circle_white)

        ssToolbar.setNavigationOnClickListener {
            // val fragment = AccountDialogFragment()
            //  fragment.show(supportFragmentManager, fragment.getTag())
        }
    }

    private fun onShareAppClick() {
        shareApp(getString(R.string.ss_menu_share_app_text))
    }

    fun shareApp(message: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            String.format("%s - %s", message, APP_PLAY_STORE_LINK)
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            // Intent ssLoginActivityIntent = new Intent(this, SSLoginActivity.class);
            //  startActivity(ssLoginActivityIntent);
        }
    }

    companion object {
        private const val APP_PLAY_STORE_LINK =
            "https://play.google.com/store/apps/details?id=com.cryart.sabbathschool"
        const val MENU_ANONYMOUS_PHOTO =
            "https://sabbath-school.adventech.io/api/v1/anonymous-photo.png"
    }
}
