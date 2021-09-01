package app.ss.media.playback.extensions

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
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
