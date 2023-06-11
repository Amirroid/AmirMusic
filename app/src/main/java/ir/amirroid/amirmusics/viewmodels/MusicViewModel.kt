package ir.amirroid.amirmusics.viewmodels

import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.amirroid.amirmusics.data.connections.MusicServiceConnection
import ir.amirroid.amirmusics.data.model.Song
import ir.amirroid.amirmusics.data.other.Resource
import ir.amirroid.amirmusics.data.repository.MusicRepository
import ir.amirroid.amirmusics.data.services.MusicService
import ir.amirroid.amirmusics.utils.Constants
import ir.amirroid.amirmusics.utils.isPlaying
import ir.amirroid.amirmusics.utils.isPlayingEnabling
import ir.amirroid.amirmusics.utils.isPreparing
import ir.amirroid.amirmusics.utils.toSong
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    @ApplicationContext val context: Context,
    private val musicRepository: MusicRepository
) : ViewModel() {
    private val _mediaItems = MutableStateFlow<Resource<List<Song>?>>(Resource.loading(emptyList()))
    val mediaItems = _mediaItems.asStateFlow()

    private val _mediaItemsSearch =
        MutableStateFlow<Resource<List<Song>?>>(Resource.loading(emptyList()))
    val mediaItemsSearch = _mediaItemsSearch.asStateFlow()


    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

//    val isConnected = musicServiceConnection.isConnected


    val currentPlayingSong = musicServiceConnection.currentSongPlaying

    val playbackState = musicServiceConnection.playbackState

    val isPlaying = musicServiceConnection.isPlaying


    val isPlayingEnabled = musicServiceConnection.isPlayingEnabled


    private val audioManager = context.applicationContext.getSystemService(AudioManager::class.java)

    val audioSessionId = musicServiceConnection.audioSessionId

    private val subscriptionCallback = MediaSubscriptionCallback()


    private val _currentSongDuration = MutableStateFlow(0f)
    val currentSongDuration = _currentSongDuration.asStateFlow()


    private val _currentSongPosition = MutableStateFlow(0f)
    val currentSongPosition = _currentSongPosition.asStateFlow()

    var searchText = mutableStateOf("")
    var isActive = mutableStateOf(false)

    init {
        viewModelScope.launch {
            _mediaItems.value = Resource.loading(null)
            musicServiceConnection.subscribe(Constants.MEDIA_ROOT_TAG, subscriptionCallback)
        }
        initializePlayerSeekbar()
        checkOfFavorite()
    }

    private fun checkOfFavorite() = viewModelScope.launch {
        currentPlayingSong.collect {
            if (it != null) {
                _isFavorite.value = musicRepository.checkExists(it.toSong())
            }
        }
    }

    private fun initializePlayerSeekbar() {
        viewModelScope.launch {
            while (true) {
                _currentSongDuration.value =
                    MusicService.currentSongDuration.coerceAtLeast(1L).toFloat()
                _currentSongPosition.value = MusicService.currentSongPosition
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        musicServiceConnection.unsubscribe(
            Constants.MEDIA_ROOT_TAG,
            object : MediaBrowserCompat.SubscriptionCallback() {})
        super.onCleared()
    }

    fun skipToNext() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPrevious() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun play() {
        musicServiceConnection.transportControls.play()
    }

    fun pause() {
        musicServiceConnection.transportControls.pause()
    }

    fun stop() {
        musicServiceConnection.transportControls.stop()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleButton(mediaItem: Song, toggleButton: Boolean = false) {
        val isPrepared = musicServiceConnection.playbackState.value?.isPreparing
        if (isPrepared == true && currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) == mediaItem.mediaId) {
            playbackState.value?.let { playbackStateCompat ->
                when {
                    playbackStateCompat.isPlaying -> if (toggleButton) musicServiceConnection.transportControls.pause()
                    playbackStateCompat.isPlayingEnabling -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }


    fun searchAudios(query: String) {
        searchText.value = query
        musicServiceConnection.search(query)
    }

    fun changeMode(mode: Int) {
        musicServiceConnection.setRepeatMode(mode)
    }

    fun changeSoundLevel(soundLevel: Float) {
        musicServiceConnection.changeSoundLevel(
            soundLevel * audioManager.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            )
        )
    }


    fun getAudioSessionId() {
        musicServiceConnection.getAudioSessionId()
    }


    fun requestFavorite(song: Song) = viewModelScope.launch {
        if (musicRepository.checkExists(song)) {
            musicRepository.deleteFavorite(song)
        } else musicRepository.insertFavorite(song)
        _isFavorite.value = musicRepository.checkExists(song)
    }

    fun shuffledMode() {
        musicServiceConnection.shuffledMode()
    }

    fun getFavorites() {
        musicServiceConnection.getFavorites()
    }

    private inner class MediaSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            val items = children.map { media ->
                Song(

                    mediaId = media.description.mediaId.toString(),
                    title = media.description.title.toString(),
                    subtitle = media.description.subtitle.toString(),
                    songUri = media.description.mediaUri!!,
                    imageUri = if (media.description.extras?.getString(Constants.IS_IMAGE) == "0") null else media.description.iconUri,
                    dateAdded = media.description.extras?.getLong(Constants.DATE_ADDED)
                        ?: Date().time
                )
            }
            if (isActive.value) {
                _mediaItemsSearch.value = Resource.success(items)
            } else {
                _mediaItems.value = Resource.success(items)
                _mediaItemsSearch.value = Resource.success(items)
            }
            getAudioSessionId()
            super.onChildrenLoaded(parentId, children)
        }
    }
}