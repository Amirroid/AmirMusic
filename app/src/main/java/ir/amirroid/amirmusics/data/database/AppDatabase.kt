package ir.amirroid.amirmusics.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.amirroid.amirmusics.data.model.Song


@TypeConverters(DataConverter::class)
@Database(entities = [Song::class], exportSchema = false, version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): FavoriteDao
}
