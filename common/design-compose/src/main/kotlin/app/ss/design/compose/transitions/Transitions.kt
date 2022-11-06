package app.ss.design.compose.transitions

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val AnimDurationLong = 500

// Enter transition when you navigate to a screen
@ExperimentalAnimationApi
fun scaleInEnterTransition() = scaleIn(
    initialScale = .9f,
    animationSpec = tween(AnimDurationLong)
) + fadeIn(
    animationSpec = tween()
)

// Exit transition when you navigate to a screen
@ExperimentalAnimationApi
fun scaleOutExitTransition() = scaleOut(
    targetScale = 1.1f,
    animationSpec = tween()
) + fadeOut(
    animationSpec = tween()
)

// Enter transition of a screen when you pop to it
@ExperimentalAnimationApi
fun scaleInPopEnterTransition() = scaleIn(
    initialScale = 1.1f,
    animationSpec = tween(AnimDurationLong)
) + fadeIn(
    animationSpec = tween()
)

// Exit transition of a screen you are popping from
@ExperimentalAnimationApi
fun scaleOutPopExitTransition() = scaleOut(
    targetScale = .9f,
    animationSpec = tween()
) + fadeOut(
    animationSpec = tween()
)

// Enter slide in transition for a screen
fun slideInTransition() = slideInHorizontally(
    initialOffsetX = { 500 }
) + fadeIn(
    animationSpec = tween()
)

// Exit slide out transition for a screen
fun slideOutTransition() = slideOutHorizontally(
    targetOffsetX = { 500 }
) + fadeOut(
    animationSpec = tween()
)
