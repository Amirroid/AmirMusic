package ir.amirroid.amirmusics.data.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.amirroid.amirmusics.utils.Constants
import java.util.Date


@Entity(tableName = Constants.FAVORITE_DATABASE)
data class Song(
    @PrimaryKey(autoGenerate = false)
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUri: Uri = Uri.EMPTY,
    val dateAdded: Long = Date().time,
    val imageUri: Uri? = Uri.EMPTY,
    val duration: Long = 0L
)
