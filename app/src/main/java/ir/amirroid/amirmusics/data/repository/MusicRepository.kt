package ir.amirroid.amirmusics.data.repository

import ir.amirroid.amirmusics.data.database.FavoriteDao
import ir.amirroid.amirmusics.data.database.MusicDataBaseHelper
import ir.amirroid.amirmusics.data.model.Song
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val databaseHelper: MusicDataBaseHelper,
    private val dao: FavoriteDao
) {
    suspend fun getAudios() = databaseHelper.getAudios()
    suspend fun insertFavorite(song: Song) = dao.insert(song)
    suspend fun checkExists(song: Song) = dao.checkExist(song.mediaId)
    suspend fun deleteFavorite(song: Song) = dao.delete(song)
    suspend fun getAllFavorites() = dao.getAllSounds()
}