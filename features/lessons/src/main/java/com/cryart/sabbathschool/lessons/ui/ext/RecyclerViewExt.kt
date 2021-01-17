package com.cryart.sabbathschool.lessons.ui.ext

import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setEdgeEffect(color: Int) {
    this.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            return EdgeEffect(view.context).apply { setColor(color) }
        }
    }
}
