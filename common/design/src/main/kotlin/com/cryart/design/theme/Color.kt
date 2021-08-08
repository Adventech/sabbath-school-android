package com.cryart.design.theme

import androidx.compose.ui.graphics.Color

val BaseBlue = Color(0xFF2E5797)
val BaseGrey1 = Color(0xFFEFEFEF)
val BaseGrey2 = Color(0xFF8F8E94)
val BaseGrey3 = Color(0xFF606060)
val BaseGrey4 = Color(0xFF383838)
val BaseGrey5 = Color(0xFF1A1A1A)
val BaseRed = Color(0xFFF1706B)

data class Colors(
    val Primary: Color = BaseBlue,
    val PrimaryVariant: Color = BaseBlue,
    val Secondary: Color = BaseBlue,
    val Background: Color = Color.White,
    val Surface: Color = Color.White,
    val OnPrimary: Color = Color.White,
    val OnSecondary: Color = Color.White,
    val OnBackground: Color = BaseGrey3,
    val OnSurface: Color = BaseGrey3,
    val Error: Color = BaseRed,
) {
    companion object {
        fun darkModeColors(): Colors = Colors(
            Primary = Color.White,
            Background = Color.Black,
            Surface = Color.Black,
            OnBackground = BaseGrey1,
            OnSurface = BaseGrey1,
            OnPrimary = BaseBlue
        )
    }
}
