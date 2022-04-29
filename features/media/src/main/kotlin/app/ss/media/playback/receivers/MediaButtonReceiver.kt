package app.ss.media.playback.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import timber.log.Timber

/**
 * Copy of androidx.media.session.MediaButtonReceiver to set FLAG_MUTABLE to pending intents.
 */
class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON != intent.action || !intent.hasExtra(Intent.EXTRA_KEY_EVENT)
        ) {
            Timber.d("Ignore unsupported intent: $intent")
            return
        }
        val mediaButtonServiceComponentName = getServiceComponentByAction(context, Intent.ACTION_MEDIA_BUTTON)
        if (mediaButtonServiceComponentName != null) {
            intent.component = mediaButtonServiceComponentName
            ContextCompat.startForegroundService(context, intent)
            return
        }
        val mediaBrowserServiceComponentName = getServiceComponentByAction(
            context,
            MediaBrowserServiceCompat.SERVICE_INTERFACE
        )
        if (mediaBrowserServiceComponentName != null) {
            val pendingResult = goAsync()
            val applicationContext = context.applicationContext
            val connectionCallback = MediaButtonConnectionCallback(applicationContext, intent, pendingResult)
            val mediaBrowser = MediaBrowserCompat(
                applicationContext,
                mediaBrowserServiceComponentName, connectionCallback, null
            )
            connectionCallback.setMediaBrowser(mediaBrowser)
            mediaBrowser.connect()
            return
        }
        throw IllegalStateException(
            "Could not find any Service that handles " +
                Intent.ACTION_MEDIA_BUTTON + " or implements a media browser service."
        )
    }

    private class MediaButtonConnectionCallback(
        private val context: Context,
        private val intent: Intent,
        private val pendingResult: PendingResult
    ) : MediaBrowserCompat.ConnectionCallback() {
        private var mediaBrowser: MediaBrowserCompat? = null

        fun setMediaBrowser(mediaBrowser: MediaBrowserCompat?) {
            this.mediaBrowser = mediaBrowser
        }

        override fun onConnected() {
            mediaBrowser?.let {
                val mediaController = MediaControllerCompat(context, it.sessionToken)
                val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                mediaController.dispatchMediaButtonEvent(event)
            }

            finish()
        }

        override fun onConnectionSuspended() {
            finish()
        }

        override fun onConnectionFailed() {
            finish()
        }

        private fun finish() {
            mediaBrowser?.disconnect()
            pendingResult.finish()
        }
    }

    companion object {

        /**
         * Creates a broadcast pending intent that will send a media button event. The `action`
         * will be translated to the appropriate [KeyEvent], and it will be sent to the
         * registered media button receiver in the given context. The `action` should be one of
         * the following:
         *
         *  * [PlaybackStateCompat.ACTION_PLAY]
         *  * [PlaybackStateCompat.ACTION_PAUSE]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_NEXT]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS]
         *  * [PlaybackStateCompat.ACTION_STOP]
         *  * [PlaybackStateCompat.ACTION_FAST_FORWARD]
         *  * [PlaybackStateCompat.ACTION_REWIND]
         *  * [PlaybackStateCompat.ACTION_PLAY_PAUSE]
         *
         *
         * @param context The context of the application.
         * @param action  The action to be sent via the pending intent.
         * @return Created pending intent, or null if cannot find a unique registered media button
         * receiver or if the `action` is unsupported/invalid.
         */
        fun buildMediaButtonPendingIntent(
            context: Context,
            @MediaKeyAction action: Long
        ): PendingIntent? {
            val mbrComponent = getMediaButtonReceiverComponent(context)
            if (mbrComponent == null) {
                Timber.w(
                    "A unique media button receiver could not be found in the given context, so couldn't build a pending intent."
                )
                return null
            }
            return buildMediaButtonPendingIntent(context, mbrComponent, action)
        }

        /**
         * Creates a broadcast pending intent that will send a media button event. The `action`
         * will be translated to the appropriate [KeyEvent], and sent to the provided media
         * button receiver via the pending intent. The `action` should be one of the following:
         *
         *  * [PlaybackStateCompat.ACTION_PLAY]
         *  * [PlaybackStateCompat.ACTION_PAUSE]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_NEXT]
         *  * [PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS]
         *  * [PlaybackStateCompat.ACTION_STOP]
         *  * [PlaybackStateCompat.ACTION_FAST_FORWARD]
         *  * [PlaybackStateCompat.ACTION_REWIND]
         *  * [PlaybackStateCompat.ACTION_PLAY_PAUSE]
         *
         *
         * @param context      The context of the application.
         * @param mbrComponent The full component name of a media button receiver where you want to send
         * this intent.
         * @param action       The action to be sent via the pending intent.
         * @return Created pending intent, or null if the given component name is null or the
         * `action` is unsupported/invalid.
         */
        fun buildMediaButtonPendingIntent(
            context: Context?,
            mbrComponent: ComponentName?,
            @MediaKeyAction action: Long
        ): PendingIntent? {
            if (mbrComponent == null) {
                Timber.w("The component name of media button receiver should be provided.")
                return null
            }
            val keyCode = PlaybackStateCompat.toKeyCode(action)
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                Timber.w(
                    "Cannot build a media button pending intent with the given action: $action"
                )
                return null
            }
            val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                component = mbrComponent
                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT

            return PendingIntent.getBroadcast(context, keyCode, intent, flags)
        }

        private fun getMediaButtonReceiverComponent(context: Context): ComponentName? {
            val queryIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            queryIntent.setPackage(context.packageName)
            val pm = context.packageManager
            val resolveInfos = pm.queryBroadcastReceivers(queryIntent, 0)
            if (resolveInfos.size == 1) {
                val resolveInfo = resolveInfos[0]
                return ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                )
            } else if (resolveInfos.size > 1) {
                Timber.w(
                    "More than one BroadcastReceiver that handles ${Intent.ACTION_MEDIA_BUTTON} was found, returning null."
                )
            }
            return null
        }

        private fun getServiceComponentByAction(context: Context, action: String): ComponentName? {
            val pm = context.packageManager
            val queryIntent = Intent(action)
            queryIntent.setPackage(context.packageName)
            val resolveInfos = pm.queryIntentServices(queryIntent, 0 /* flags */)
            return when {
                resolveInfos.size == 1 -> {
                    val resolveInfo = resolveInfos[0]
                    ComponentName(
                        resolveInfo.serviceInfo.packageName,
                        resolveInfo.serviceInfo.name
                    )
                }
                resolveInfos.isEmpty() -> {
                    null
                }
                else -> {
                    throw IllegalStateException(
                        "Expected 1 service that handles " + action + ", found " +
                            resolveInfos.size
                    )
                }
            }
        }
    }
}
