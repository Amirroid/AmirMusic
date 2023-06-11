package ir.amirroid.amirmusics.data.connections

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.amirroid.amirmusics.data.other.Event
import ir.amirroid.amirmusics.data.other.Resource
import ir.amirroid.amirmusics.data.services.MusicService
import ir.amirroid.amirmusics.utils.Constants
import ir.amirroid.amirmusics.utils.isPlaying
import ir.amirroid.amirmusics.utils.isPlayingEnabling
import ir.amirroid.amirmusics.utils.isStopped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MusicServiceConnection @Inject constructor(
    @ApplicationContext context: Context
) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean?>>>()
    val isConnected: LiveData<Event<Resource<Boolean?>>> = _isConnected


    private val _playbackState =
        MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying = _isPlaying.asStateFlow()

    private val _audioSessionId = MutableStateFlow(0)
    val audioSessionId = _audioSessionId.asStateFlow()

    private val _isPlayingEnabled = MutableStateFlow(false)
    val isPlayingEnabled = _isPlayingEnabled.asStateFlow()


    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)


    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }


    fun subscribe(parentID: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentID, callback)
    }

    fun unsubscribe(parentID: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentID, callback)
    }

    fun search(query: String) {
        mediaBrowser.sendCustomAction(
            Constants.MEDIA_SEARCH_TAG,
            Bundle().apply { putString(Constants.SEARCH_TAG, query) }, null
        )
    }

    fun refreshData() {
        mediaBrowser.sendCustomAction(
            Constants.REFRESH_TAG, Bundle(), null
        )
    }


    fun setRepeatMode(mode: Int) {
        mediaBrowser.sendCustomAction(
            Constants.REPEAT_MODE,
            Bundle().apply { putInt(Constants.REPEAT_MODE, mode) },
            null
        )
    }

    fun shuffledMode() {
        mediaBrowser.sendCustomAction(
            Constants.SHUFFLED_MODE,
            Bundle(),
            null
        )
    }

    fun getFavorites() {
        mediaBrowser.sendCustomAction(
            Constants.FAVORITE_MODE,
            Bundle(),
            null
        )
    }

    fun changeSoundLevel(soundLevel: Float) {
        mediaController.setVolumeTo((soundLevel).toInt(), AudioManager.FLAG_VIBRATE)
    }

    fun getAudioSessionId() {
        mediaBrowser.sendCustomAction(
            Constants.GET_AUDIO_SESSION,
            Bundle(),
            object : MediaBrowserCompat.CustomActionCallback() {
                override fun onResult(action: String?, extras: Bundle?, resultData: Bundle?) {
                    Log.d(
                        "dsuahfie",
                        "onCustomAction: ${resultData?.getInt(Constants.GET_AUDIO_SESSION)}"
                    )
                    _audioSessionId.value = resultData?.getInt(Constants.GET_AUDIO_SESSION) ?: 0
                    Log.d("dsuahfie", "onCustomAction: ${audioSessionId.value}")
                    super.onResult(action, extras, resultData)
                }
            }
        )
    }

    private val _currentSongPlaying = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentSongPlaying = _currentSongPlaying.asStateFlow()

    private lateinit var mediaController: MediaControllerCompat


    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls


    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _isPlaying.value = state?.isPlaying ?: false
            _playbackState.value = state
            _isPlayingEnabled.value = state.isStopped.not()
            super.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentSongPlaying.value = metadata
            super.onMetadataChanged(metadata)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
            super.onSessionDestroyed()
        }
    }


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            refreshData()
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            changeSoundLevel(1f)
            _isConnected.postValue(Event(Resource.success(true)))
            super.onConnected()
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(Resource.error("connect is problem")))
            _currentSongPlaying.value = null
            super.onConnectionSuspended()
        }
    }
}