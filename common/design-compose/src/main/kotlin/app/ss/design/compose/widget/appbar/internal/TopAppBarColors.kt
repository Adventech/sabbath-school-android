package app.ss.design.compose.widget.appbar.internal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.applyTonalElevation

@Composable
fun topAppBarColors(
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    scrolledContainerColor: Color = MaterialTheme.colorScheme.applyTonalElevation(
        backgroundColor = containerColor,
        elevation = 4.dp
    ),
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    titleContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
): TopAppBarColors {
    return remember(
        containerColor,
        scrolledContainerColor,
        navigationIconContentColor,
        titleContentColor,
        actionIconContentColor
    ) {
        AnimatingTopAppBarColors(
            containerColor,
            scrolledContainerColor,
            navigationIconContentColor,
            titleContentColor,
            actionIconContentColor
        )
    }
}

@Stable
private class AnimatingTopAppBarColors(
    private val containerColor: Color,
    private val scrolledContainerColor: Color,
    navigationIconContentColor: Color,
    titleContentColor: Color,
    actionIconContentColor: Color
) : TopAppBarColors {

    // In this TopAppBarColors implementation, the following colors never change their value as the
    // app bar scrolls.
    private val navigationIconColorState: State<Color> = mutableStateOf(navigationIconContentColor)
    private val titleColorState: State<Color> = mutableStateOf(titleContentColor)
    private val actionIconColorState: State<Color> = mutableStateOf(actionIconContentColor)

    @Composable
    override fun containerColor(scrollFraction: Float): State<Color> {
        return animateColorAsState(
            // Check if scrollFraction is slightly over zero to overcome float precision issues.
            targetValue = if (scrollFraction > 0.01f) {
                scrolledContainerColor
            } else {
                containerColor
            },
            animationSpec = tween(
                durationMillis = TopAppBarAnimationDurationMillis,
                easing = LinearOutSlowInEasing
            )
        )
    }

    @Composable
    override fun navigationIconContentColor(scrollFraction: Float): State<Color> =
        navigationIconColorState

    @Composable
    override fun titleContentColor(scrollFraction: Float): State<Color> = titleColorState

    @Composable
    override fun actionIconContentColor(scrollFraction: Float): State<Color> = actionIconColorState
}

private const val TopAppBarAnimationDurationMillis = 500
