package ir.amirroid.amirmusics.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ir.amirroid.amirmusics.data.model.Song
import ir.amirroid.amirmusics.utils.Constants

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM ${Constants.FAVORITE_DATABASE}")
    suspend fun getAllSounds(): List<Song>


    @Insert
    suspend fun insert(song: Song)

    @Delete
    suspend fun delete(song: Song)


    @Query("SELECT EXISTS(SELECT * FROM ${Constants.FAVORITE_DATABASE} WHERE mediaId = :id)")
    suspend fun checkExist(id: String): Boolean
}