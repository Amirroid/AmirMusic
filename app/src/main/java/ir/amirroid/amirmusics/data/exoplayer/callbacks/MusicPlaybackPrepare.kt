package ir.amirroid.amirmusics.data.exoplayer.callbacks

import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import ir.amirroid.amirmusics.data.source.LocaleMusicSource

class MusicPlaybackPrepare(
    private val source: LocaleMusicSource,
    private val onPrepareMusic: (MediaMetadataCompat?) -> Unit
) : MediaSessionConnector.PlaybackPreparer {
    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = true

    override fun getSupportedPrepareActions(): Long {
        return PlaybackState.ACTION_PREPARE_FROM_MEDIA_ID or PlaybackState.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        source.whenReady {
            val itemToPlay = source.songs.find { it.description.mediaId == mediaId }
            onPrepareMusic.invoke(itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

}