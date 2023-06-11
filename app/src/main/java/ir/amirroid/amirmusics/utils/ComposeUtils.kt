package ir.amirroid.amirmusics.utils

import android.annotation.SuppressLint
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@SuppressLint("ComposableNaming", "CoroutineCreationDuringComposition")
@Composable
fun StateFlow<Float>.collectAsStateWithLifecycleWithAnimation(scope: CoroutineScope = rememberCoroutineScope()): Animatable<Float, AnimationVector1D> {
    val animationValue = remember {
        Animatable(value)
    }
    scope.launch {
        collect {
            animationValue.animateTo(it)
        }
    }
    return animationValue
}