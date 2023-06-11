package ir.amirroid.amirmusics.data.database

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

class DataConverter {
    @TypeConverter
    fun uriToString(uri: Uri): String {
        return uri.toString()
    }
    @TypeConverter
    fun stringToUri(string: String): Uri {
        return string.toUri()
    }
}