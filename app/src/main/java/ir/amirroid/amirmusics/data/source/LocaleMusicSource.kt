package ir.amirroid.amirmusics.data.source

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import ir.amirroid.amirmusics.data.database.MusicDataBaseHelper
import ir.amirroid.amirmusics.data.repository.MusicRepository
import ir.amirroid.amirmusics.utils.Constants
import ir.amirroid.amirmusics.utils.asMediaMetaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class LocaleMusicSource @Inject constructor(private val repository: MusicRepository) {

    var songs = emptyList<MediaMetadataCompat>()
    private var allSongs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = MusicState.STATE_INITIALIZING
        val songsI = repository.getAudios()
        songs = songsI.map { song ->
            song.asMediaMetaData()
        }
        delay(50)
        allSongs = songs
        state = MusicState.STATE_INITIALIZED
    }

    fun shuffle() {
        songs = songs.shuffled()
    }

    suspend fun fetchMediaDataWithSearch(title: String) = withContext(Dispatchers.IO) {
        state = MusicState.STATE_INITIALIZING
        songs = if (title.isNotEmpty()) {
            allSongs.filter {
                it.getString(MediaMetadataCompat.METADATA_KEY_TITLE).contains(title, true)
            }
        } else {
            allSongs
        }
        state = MusicState.STATE_INITIALIZED
    }

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: MusicState = MusicState.STATE_CREATED
        set(value) {
            if (value == MusicState.STATE_INITIALIZED || value == MusicState.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener.invoke(state == MusicState.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == MusicState.STATE_CREATED || state == MusicState.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == MusicState.STATE_INITIALIZED)
            true
        }
    }

    fun asMediaSource(dataSource: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {
            val mediaSource = ProgressiveMediaSource.Factory(dataSource)
                .createMediaSource(
                    MediaItem.fromUri(
                        it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()
                    )
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map {
        val description = it.description
        val desc = MediaDescriptionCompat.Builder()
            .setTitle(description.title)
            .setIconUri(if (it.getString(Constants.IS_IMAGE) == "0") null else description.iconUri)
            .setSubtitle(description.subtitle)
            .setMediaId(description.mediaId)
            .setMediaUri(description.mediaUri)
            .setExtras(Bundle().apply {
                putLong(
                    Constants.DATE_ADDED,
                    getLong(Constants.DATE_ADDED)
                )
            })
            .build()
        MediaBrowserCompat.MediaItem(
            desc,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }.toMutableList()

    suspend fun refresh() {
        onReadyListeners.clear()
        fetchMediaData()
    }

    suspend fun getFavorites() = withContext(Dispatchers.IO) {
        state = MusicState.STATE_INITIALIZING
        val songsI = repository.getAllFavorites()
        songs = songsI.map { song ->
            song.asMediaMetaData()
        }
        state = MusicState.STATE_INITIALIZED
    }
}

enum class MusicState {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}