package ir.amirroid.amirmusics.data.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import ir.amirroid.amirmusics.data.exoplayer.MusicNotificationManager
import ir.amirroid.amirmusics.data.exoplayer.callbacks.MusicPlaybackPrepare
import ir.amirroid.amirmusics.data.exoplayer.callbacks.MusicPlayerEventListener
import ir.amirroid.amirmusics.data.exoplayer.callbacks.MusicPlayerNotificationListener
import ir.amirroid.amirmusics.data.source.LocaleMusicSource
import ir.amirroid.amirmusics.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
    @Inject
    lateinit var dataStoreFactory: DefaultDataSource.Factory


    @Inject
    lateinit var exoPlayer: ExoPlayer


    @Inject
    lateinit var localeMusicSource: LocaleMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var mediaSessions: MediaSessionCompat

    private lateinit var mediaSessionsConnector: MediaSessionConnector

    private var currentPlayingSong: MediaMetadataCompat? = null

    var isForegroundService = false


    private lateinit var musicEventListener: MusicPlayerEventListener


    private val mainHandler = Handler(Looper.getMainLooper())

    private var isPlayerInitialized = false

    companion object {
        var currentSongDuration = 0L
            private set
        var currentSongPosition = 0f
            private set
    }

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            localeMusicSource.fetchMediaData()
        }

        val activity = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSessions = MediaSessionCompat(this.applicationContext, Constants.SERVICE_TAG).apply {
            setSessionActivity(activity)
            isActive = true
        }
        sessionToken = mediaSessions.sessionToken


        val playbackPrepare = MusicPlaybackPrepare(localeMusicSource) {
            currentPlayingSong = it
            preparePlayer(localeMusicSource.songs, currentPlayingSong)
        }

        mediaSessionsConnector = MediaSessionConnector(mediaSessions)
        mediaSessionsConnector.setPlaybackPreparer(playbackPrepare)
        mediaSessionsConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionsConnector.setPlayer(exoPlayer)

        musicNotificationManager =
            MusicNotificationManager(
                this,
                mediaSessions.sessionToken,
                MusicPlayerNotificationListener(this)
            ) {
                currentSongDuration = exoPlayer.duration
            }

        musicEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicEventListener)
        musicNotificationManager.showNotification(exoPlayer)
        scope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    currentSongPosition = exoPlayer.currentPosition.coerceAtLeast(0).toFloat()
                }
                delay(1000)
            }
        }
    }

    @Synchronized
    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        isPlayNow: Boolean = true
    ) {
        val currentSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        mainHandler.post {
            exoPlayer.setMediaSource(localeMusicSource.asMediaSource(dataStoreFactory))
            exoPlayer.seekTo(currentSongIndex, 0L)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlayNow
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(Constants.MEDIA_ROOT_TAG, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaItem>>
    ) {

        when (parentId) {
            Constants.MEDIA_ROOT_TAG -> {
                val resultSend = localeMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(localeMusicSource.asMediaItems())
                        if (!isPlayerInitialized && localeMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                localeMusicSource.songs,
                                null
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        result.sendResult(null)
                    }
                }
                if (!resultSend) {
                    scope.launch {

                        localeMusicSource.refresh()
                    }
                    result.detach()
                }
            }

            else -> Unit
        }
    }


    override fun onDestroy() {
        scope.cancel()
        exoPlayer.release()
        exoPlayer.removeListener(musicEventListener)
        super.onDestroy()
    }

    override fun onCustomAction(action: String, extras: Bundle?, result: Result<Bundle>) {
        when (action) {
            Constants.MEDIA_SEARCH_TAG -> {
                val searchQuery = extras?.getString(Constants.SEARCH_TAG) ?: ""
                scope.launch {
                    localeMusicSource.fetchMediaDataWithSearch(searchQuery)
                    notifyChildrenChanged(Constants.MEDIA_ROOT_TAG)
                }
            }

            Constants.REFRESH_TAG -> {
                scope.launch {
                    localeMusicSource.refresh()
                    notifyChildrenChanged(Constants.MEDIA_ROOT_TAG)
                }
            }

            Constants.REPEAT_MODE -> {
                val mode = extras?.getInt(Constants.REPEAT_MODE) ?: ExoPlayer.REPEAT_MODE_ALL
                exoPlayer.repeatMode = mode
            }

            Constants.SHUFFLED_MODE -> {
                localeMusicSource.shuffle()
                exoPlayer.setMediaSource(localeMusicSource.asMediaSource(dataStoreFactory))
                exoPlayer.play()
                notifyChildrenChanged(Constants.MEDIA_ROOT_TAG)
            }

            Constants.GET_AUDIO_SESSION -> {
                val res =
                    Bundle().apply {
                        putInt(
                            Constants.GET_AUDIO_SESSION,
                            exoPlayer.audioSessionId
                        )
                    }
                result.sendResult(res)
                return
            }

            Constants.FAVORITE_MODE -> {
                scope.launch {
                    localeMusicSource.getFavorites()
                    currentPlayingSong =
                        localeMusicSource.songs.find { it.description.mediaId == exoPlayer.currentMediaItem?.mediaId }
                    notifyChildrenChanged(Constants.MEDIA_ROOT_TAG)
                }
            }

            else -> Unit
        }
        super.onCustomAction(action, extras, result)
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        exoPlayer.stop()
        musicNotificationManager.hideNotification()
        super.onTaskRemoved(rootIntent)
    }

    inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSessions) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return localeMusicSource.songs[windowIndex].description
        }

    }
}