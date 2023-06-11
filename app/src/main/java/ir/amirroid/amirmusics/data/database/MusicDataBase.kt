package ir.amirroid.amirmusics.data.database

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.amirroid.amirmusics.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import javax.inject.Inject

class MusicDataBaseHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    private var cursor: Cursor? = null

    private val idC = MediaStore.Audio.Media._ID
    private val titleC = MediaStore.Audio.Media.TITLE
    private val artistC = MediaStore.Audio.Media.ARTIST
    private val dateC = MediaStore.Audio.Media.DATE_ADDED
    private val albumC = MediaStore.Audio.Media.ALBUM_ID
    private val durationsC = MediaStore.Audio.Media.DURATION
    private val projection = arrayOf(
        idC,
        titleC,
        artistC,
        dateC,
        albumC,
        durationsC
    )
    private val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

    @WorkerThread
    suspend fun getAudios() = withContext(Dispatchers.IO) {
        return@withContext try {
            getAllAudios()
        } catch (_: Exception) {
            emptyList<Song>()
        }
    }

    private fun getAllAudios(): List<Song> {
        val list = mutableListOf<Song>()
        cursor = context.applicationContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            sort
        )
        if (cursor != null) {
            cursor.use {
                val durationT = it!!.getColumnIndexOrThrow(durationsC)
                val albumIdT = it.getColumnIndexOrThrow(albumC)
                val titleT = it.getColumnIndexOrThrow(titleC)
                val dateT = it.getColumnIndexOrThrow(dateC)
                val artistT = it.getColumnIndexOrThrow(artistC)
                val idT = it.getColumnIndexOrThrow(idC)
                if (cursor!!.moveToFirst()) {
                    do {
                        val id = it.getLong(idT)
                        val artist = it.getString(artistT)
                        val date = it.getLong(dateT)
                        val title = it.getString(titleT)
                        val albumId = it.getLong(albumIdT)
                        val duration = it.getLong(durationT)
                        val songUri = ContentUris.withAppendedId(
                            uri,
                            id
                        )
                        val imageUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )
                        Log.d(
                            "TAGMUSIC",
                            "getAllAudiosWithSearch: ${SimpleDateFormat("yyyy/MM/dd").format(date)}"
                        )
                        list.add(
                            Song(
                                id.toString(),
                                title,
                                artist,
                                songUri,
                                date,
                                imageUri,
                                duration
                            )
                        )
                    } while (cursor!!.moveToNext())
                }
            }
        }
        return list
    }


//    @WorkerThread
//    suspend fun getAudiosWithSearch(title: String) =
//        withContext(Dispatchers.IO) { getAllAudiosWithSearch(title) }
//
//    private fun getAllAudiosWithSearch(title: String): List<Song> {
//        val list = mutableListOf<Song>()
//        cursor = context.applicationContext.contentResolver.query(
//            uri,
//            projection,
//            "${MediaStore.Audio.Media.IS_MUSIC} =? AND $titleC LIKE '%${title}%'",
//            selectionArgs,
//            sort
//        )
//        if (cursor != null) {
//            if (cursor!!.moveToFirst()) {
//                while (cursor!!.moveToNext()) {
//                    val id = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(idC))
//                    val data = cursor!!.getString(cursor!!.getColumnIndexOrThrow(dataC))
//                    val date = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(dateC))
//                    val title = cursor!!.getString(cursor!!.getColumnIndexOrThrow(titleC))
//                    val albumId = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(albumC))
//                    val duration = cursor!!.getLong(cursor!!.getColumnIndexOrThrow(durationsC))
//                    val songUri = ContentUris.withAppendedId(
//                        uri,
//                        id
//                    )
//                    Log.d("album_id", "getAllAudiosWithSearch: $albumId")
//                    val imageUri = ContentUris.withAppendedId(
//                        Uri.parse("content://media/external/audio/albumart"),
//                        albumId
//                    )
//                    list.add(
//                        Song(
//                            id.toString(),
//                            title,
//                            data,
//                            songUri,
//                            date,
//                            if (checkImageUri(imageUri)) imageUri else null,
//                            duration
//                        )
//                    )
//                }
//            }
//        }
//        return list
//    }
}