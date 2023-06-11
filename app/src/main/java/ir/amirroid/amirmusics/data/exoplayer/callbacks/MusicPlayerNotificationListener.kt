package ir.amirroid.amirmusics.data.exoplayer.callbacks

import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import ir.amirroid.amirmusics.data.services.MusicService
import ir.amirroid.amirmusics.utils.Constants

class MusicPlayerNotificationListener(
    private val musicService: MusicService
) : NotificationListener {
    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        musicService.apply {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(Constants.NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
        super.onNotificationPosted(notificationId, notification, ongoing)
    }

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        musicService.apply {
            isForegroundService = false
            stopSelf()
        }
        super.onNotificationCancelled(notificationId, dismissedByUser)
    }
}