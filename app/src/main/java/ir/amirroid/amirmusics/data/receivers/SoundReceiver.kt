package ir.amirroid.amirmusics.data.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest


const val TAG = "VOLUMETAGAPP"

class SoundReceiver : BroadcastReceiver() {
    val soundLevel = MutableStateFlow(0f)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
            val manager = context?.getSystemService(AudioManager::class.java) ?: return
            val volumeLevel = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxMusicLevel = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val percent = volumeLevel / maxMusicLevel.toFloat()
            Log.d(TAG, "onReceive: $volumeLevel")
            Log.d(TAG, "onReceive: $maxMusicLevel")
            Log.d(TAG, "onReceive: $percent")
            soundLevel.value = percent
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun startBroadcast(context: Context, onVolumeChange: (Float) -> Unit) {
    val receiver = SoundReceiver()
    LaunchedEffect(Unit) {
        receiver.soundLevel.collectLatest {
            onVolumeChange.invoke(it)
        }
    }
    DisposableEffect(Unit) {
        context.registerReceiver(receiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}