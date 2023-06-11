package ir.amirroid.amirmusics.utils

import android.content.Context
import android.media.AudioManager
import kotlin.math.pow
import kotlin.math.roundToInt

fun Float.round(digits: Int, f: Float = 10f): Float {
    val factor = f.pow(digits)
    return (this * factor).roundToInt() / factor
}

fun Int.round(digits: Int, f: Float = 10f): Float {
    val factor = f.pow(digits)
    return (this * factor).roundToInt() / factor
}


fun getSound(context: Context): Float {
    val manager = context.getSystemService(AudioManager::class.java)
    return (manager.getStreamVolume(AudioManager.STREAM_MUSIC) / manager.getStreamMaxVolume(
        AudioManager.STREAM_MUSIC
    )).round(1)
}