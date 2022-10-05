package com.cryart.sabbathschool.core.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.cryart.sabbathschool.core.R

abstract class SlidingActivity : SSBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.enter_from_right, android.R.anim.fade_out)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.exit_to_right)
    }
}
