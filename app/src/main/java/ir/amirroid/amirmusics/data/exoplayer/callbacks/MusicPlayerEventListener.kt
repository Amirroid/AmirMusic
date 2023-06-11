package ir.amirroid.amirmusics.data.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import ir.amirroid.amirmusics.data.services.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            musicService.stopForeground(false)
        }
        super.onPlaybackStateChanged(playbackState)
    }

    override fun onPlayerError(error: PlaybackException) {
        Toast.makeText(musicService.applicationContext, error.message, Toast.LENGTH_LONG).show()
        super.onPlayerError(error)
    }

    override fun onVolumeChanged(volume: Float) {

        super.onVolumeChanged(volume)
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        super.onAudioSessionIdChanged(audioSessionId)
    }
}