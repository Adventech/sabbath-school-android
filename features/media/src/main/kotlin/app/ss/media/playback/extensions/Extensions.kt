package app.ss.media.playback.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.util.concurrent.TimeUnit

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun Long.millisToDuration(): String {
    val seconds = TimeUnit.SECONDS.convert(this, TimeUnit.MILLISECONDS).toInt()
    val minutes = TimeUnit.MINUTES.convert(this, TimeUnit.MILLISECONDS).toInt()
    val hours = TimeUnit.HOURS.convert(this, TimeUnit.MILLISECONDS).toInt()

    "${timeAddZeros(hours)}:${timeAddZeros(minutes, "0")}:${timeAddZeros(seconds, "00")}".apply {
        return if (startsWith(":")) replaceFirst(":", "") else this
    }
}

fun timeAddZeros(number: Int?, ifZero: String = ""): String {
    return when (number) {
        0 -> ifZero
        else -> number?.toString()?.padStart(2, '0') ?: "00"
    }
}

suspend fun Context.getBitmap(uri: Uri, size: Int): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(uri)
        .size(size)
        .precision(coil.size.Precision.INEXACT)
        .allowHardware(true)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        else -> null
    }
}
