package ir.amirroid.amirmusics.ui.components

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.amirroid.amirmusics.utils.round

@Composable
fun EqualizerDialog(context: Context, audioSessionId: Int, onDismissRequest: () -> Unit) {
    if (audioSessionId != 0) {
        val equalizer = Equalizer(0, audioSessionId)
        val isEnable = remember {
            mutableStateOf(equalizer.enabled)
        }
        DisposableEffect(key1 = Unit) {
            customEqualizerEnable(context, audioSessionId)
            onDispose { equalizer.release() }
        }
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(modifier = Modifier.fillMaxHeight(0.7f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Checkbox(checked = isEnable.value, onCheckedChange = {
                        isEnable.value = it
                        equalizer.enabled = it
                    })
                    val bands = equalizer.numberOfBands
                    val minEqLevel = equalizer.bandLevelRange[0]
                    val maxEqLevel = equalizer.bandLevelRange[1]
                    for (i in 0 until bands) {
                        val band: Short = i.toShort()
                        val freqEq = equalizer.getCenterFreq(band).div(1000).toFloat()
                        val boundLevel = equalizer.getBandLevel(band)
                        Column {
                            EqualizerSlider(
                                onValueChanged = {
                                    try {
                                        equalizer.setBandLevel(
                                            band,
                                            (it + minEqLevel).toInt().toShort()
                                        )
                                        true
                                    } catch (e: Exception) {
                                        false
                                    }
                                },
                                minEqLevel = minEqLevel,
                                maxEqLevel = maxEqLevel,
                                freqEq = freqEq,
                                context = context,
                                boundLevel = boundLevel,
                                enabled = isEnable.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EqualizerSlider(
    onValueChanged: (Float) -> Boolean,
    minEqLevel: Short,
    maxEqLevel: Short,
    freqEq: Float,
    boundLevel: Short,
    context: Context,
    enabled: Boolean
) {
    val value = remember {
        mutableStateOf(boundLevel.toFloat())
    }
    Column {
//        VerticalSlider(
//            value = value.value,
//            onValueChange = {
//                value.value = it
//                onValueChanged.invoke((it * max) + minEqLevel)
//            },
//            context = context,
//        )
        Slider(value = value.value, onValueChange = {
            if (onValueChanged.invoke(it)) {
                value.value = it
            }
        }, valueRange = 0f..maxEqLevel.minus(minEqLevel).toFloat(), enabled = enabled)
//        Text(text = freqEq.round(1).toString() + " Hz")
//        Text(text = minEqLevel.div(100).round(1).toString() + " db")
//        Text(text = maxEqLevel.div(100).round(1).toString() + " db")
    }
}

private fun customEqualizerEnable(context: Context, audioSessionId: Int) {
    val intent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
    intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
    intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
    context.sendBroadcast(intent)
}