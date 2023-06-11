package ir.amirroid.amirmusics.utils

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import ir.amirroid.amirmusics.data.model.Song
import java.util.Date


fun Song.asMediaMetaData(): MediaMetadataCompat {
    val song = this
    return MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
        .putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
            song.imageUri.toString()
        )
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUri.toString())
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.songUri.toString())
        .putLong(Constants.DATE_ADDED, song.dateAdded)
        .build().apply {
            description.extras?.putLong(Constants.DATE_ADDED, song.dateAdded)
        }
}


fun MediaMetadataCompat.toSong(): Song {
    val media = this
    return Song(
        media.description.mediaId.toString(),
        media.description.title.toString(),
        media.description.subtitle.toString(),
        media.description.mediaUri ?: Uri.EMPTY,
        media.description.extras?.getLong(
            Constants.DATE_ADDED,
        ) ?: Date().time,
        media.description.iconUri
    )
}


inline val PlaybackStateCompat.isPreparing: Boolean
    get() = state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING || state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING

inline val PlaybackStateCompat.isPlayingEnabling
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L && state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat?.isStopped
    get() = this == null || state == PlaybackStateCompat.STATE_STOPPED


fun formatTime(time: Float): String {
    val nTime = time.div(1000).toInt().toFloat()
    val seconds = (nTime % 60).toInt().toString()
    val minute = (nTime / 60).toInt().toString()
    val hour = (nTime / 3600).toInt().toString()
    return "${if (hour.hasZero) "" else if (hour.length == 1) "0${hour}:" else "${hour}:"}${if (minute.hasZero) "00:" else if (minute.length == 1) "0${minute}:" else "${minute}:"}${if (seconds.hasZero) "00" else if (seconds.length == 1) "0${seconds}" else seconds}"
}
