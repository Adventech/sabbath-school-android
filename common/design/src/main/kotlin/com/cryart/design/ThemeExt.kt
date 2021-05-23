package com.cryart.design

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicatorSpec

internal const val PRIMARY_COLOR_ALPHA = 90

fun BaseProgressIndicator<LinearProgressIndicatorSpec>.theme(@ColorInt color: Int) {
    setIndicatorColor(color)
    trackColor = ColorUtils.setAlphaComponent(color, PRIMARY_COLOR_ALPHA)
}
