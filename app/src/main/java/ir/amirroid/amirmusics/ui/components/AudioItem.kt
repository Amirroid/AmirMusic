package ir.amirroid.amirmusics.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import ir.amirroid.amirmusics.R
import ir.amirroid.amirmusics.data.model.Song
import ir.amirroid.amirmusics.utils.isEmpty
import ir.amirroid.amirmusics.utils.isNotEmpty

@Composable
fun AudioItem(song: Song, isSelect: Boolean, context: Context, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text = song.title) },
        supportingContent = {
            Text(
                text = song.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        },
        leadingContent = {
            AsyncImage(
                model =
                ImageRequest.Builder(context)
                    .crossfade(true)
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .crossfade(200)
                    .data(
                        if (song.imageUri.isNotEmpty) song.imageUri else R.drawable.img
                    )
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.medium),
                filterQuality = FilterQuality.Low,
                contentScale = ContentScale.Crop
            )
        },
        modifier = Modifier
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.primary.copy(if (isSelect) 0.08f else 0f)),
        tonalElevation = if (isSelect) 4.dp else 0.dp
    )
}