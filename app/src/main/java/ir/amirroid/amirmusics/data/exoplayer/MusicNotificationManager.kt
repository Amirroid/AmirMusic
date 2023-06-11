package ir.amirroid.amirmusics.data.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.session.MediaController
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat.Token
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import ir.amirroid.amirmusics.R
import ir.amirroid.amirmusics.utils.Constants

class MusicNotificationManager(
    private val context: Context,
    private val sessionToken: Token,
    notificationListener: NotificationListener,
    private val newSongCallback: (String?) -> Unit
) {
    private val notificationManager: PlayerNotificationManager
    private val mediaController = MediaControllerCompat(context, sessionToken)
    private val descriptionAdapter = DescriptionAdapter(mediaController)

    init {
        notificationManager = PlayerNotificationManager.Builder(
            context,
            Constants.NOTIFICATION_ID,
            Constants.NOTIFICATION_CHANNEL_ID,
        ).apply {
            setMediaDescriptionAdapter(descriptionAdapter)
            setNotificationListener(notificationListener)
        }.build().apply {
            setMediaSessionToken(sessionToken)
            setSmallIcon(R.drawable.round_music_note_24)
        }
    }


    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    private inner class DescriptionAdapter(private val mediaController: MediaControllerCompat) :
        MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback.invoke(player.currentMediaItem?.mediaId)
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(mediaController.metadata.description.iconUri.toString().toUri())
                .target {
                    callback.onBitmap((it as BitmapDrawable).bitmap)
                }
            loader.enqueue(request.build())
            return null
        }
    }
}