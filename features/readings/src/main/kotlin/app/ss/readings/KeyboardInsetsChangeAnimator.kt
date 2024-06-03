package app.ss.readings

import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars

/**
 * Synchronizes keyboard entry & exit animation with the app's layout.
 */
class KeyboardInsetsChangeAnimator(
    private val layout: ViewGroup
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE),
    OnApplyWindowInsetsListener {

    private lateinit var lastWindowInsets: WindowInsetsCompat
    private var isKeyboardAnimating = false

    private var isTemporarilyDisabled = false

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (!isTemporarilyDisabled && animation.typeMask and ime() != 0) {
            isKeyboardAnimating = true
        }
    }

    override fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        lastWindowInsets = insets

        // When the keyboard isn't animating, the insets are applied immediately.
        // Otherwise, they're applied during each frame of the animation in onProgress().
        if (!isKeyboardAnimating) {
            layout.setPadding(insets)
        }

        // Stop the insets being dispatched any further into the view hierarchy.
        return WindowInsetsCompat.CONSUMED
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        if (isKeyboardAnimating) {
            layout.setPadding(insets)
        }
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (isKeyboardAnimating && (animation.typeMask and ime() != 0)) {
            isKeyboardAnimating = false
        }
        isTemporarilyDisabled = false
    }

    private fun View.setPadding(windowInsets: WindowInsetsCompat) {
        windowInsets.getInsets(systemBars() or ime()).let {
            setPadding(it.left, 0, it.right, it.bottom)
        }
    }
}
