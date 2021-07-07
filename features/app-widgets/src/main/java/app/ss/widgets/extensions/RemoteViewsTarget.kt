package app.ss.widgets.extensions

import android.graphics.drawable.Drawable
import coil.target.Target

class RemoteViewsTarget(private val onResource: (Drawable?) -> Unit) : Target {

    override fun onError(error: Drawable?) {
        super.onError(error)
        onResource(error)
    }

    override fun onSuccess(result: Drawable) {
        super.onSuccess(result)
        onResource(result)
    }

    override fun onStart(placeholder: Drawable?) {
        super.onStart(placeholder)
        onResource(placeholder)
    }
}
