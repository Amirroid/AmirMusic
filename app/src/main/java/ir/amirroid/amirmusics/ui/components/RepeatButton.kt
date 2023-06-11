package ir.amirroid.amirmusics.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.google.android.exoplayer2.ExoPlayer
import ir.amirroid.amirmusics.R

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun RepeatButton(onChangeMode: (mode: Int) -> Unit) {
    val modes = listOf(
        Pair(R.drawable.round_repeat_24, ExoPlayer.REPEAT_MODE_ALL),
        Pair(R.drawable.round_repeat_one_24, ExoPlayer.REPEAT_MODE_ONE),
    )
    val thisMode = remember {
        mutableStateOf(modes.first())
    }
    IconButton(onClick = {
        if (modes.indexOf(thisMode.value) == 0) {
            thisMode.value = modes.last()
        } else thisMode.value = modes.first()
        onChangeMode.invoke(thisMode.value.second)
    }) {
        Crossfade(targetState = thisMode.value, label = "") {
            Icon(painter = painterResource(id = thisMode.value.first), contentDescription = null)
        }
    }
}