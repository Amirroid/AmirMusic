package ir.amirroid.amirmusics.ui.features.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import ir.amirroid.amirmusics.R
import ir.amirroid.amirmusics.data.model.Song
import ir.amirroid.amirmusics.data.other.Resource
import ir.amirroid.amirmusics.data.other.Status
import ir.amirroid.amirmusics.data.receivers.startBroadcast
import ir.amirroid.amirmusics.ui.BarVisualizer
import ir.amirroid.amirmusics.ui.components.AudioItem
import ir.amirroid.amirmusics.ui.components.EqualizerDialog
import ir.amirroid.amirmusics.ui.components.ImageAndSoundForSong
import ir.amirroid.amirmusics.ui.components.RepeatButton
import ir.amirroid.amirmusics.ui.components.VisualizerView
import ir.amirroid.amirmusics.utils.collectAsStateWithLifecycleWithAnimation
import ir.amirroid.amirmusics.utils.formatTime
import ir.amirroid.amirmusics.utils.getSound
import ir.amirroid.amirmusics.utils.isPlayingEnabling
import ir.amirroid.amirmusics.utils.round
import ir.amirroid.amirmusics.utils.toSong
import ir.amirroid.amirmusics.viewmodels.MusicViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel: MusicViewModel = hiltViewModel()
    val songs by viewModel.mediaItems.collectAsStateWithLifecycle()
    val songsSearch by viewModel.mediaItemsSearch.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val search by viewModel.searchText
    val isActive by viewModel.isActive
    val offsetSearchbar = remember {
        Animatable(12f)
    }
    var soundLevel by remember {
        mutableStateOf(getSound(context))
    }
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingMusic by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
        )
    )
    var isFavoriteMode by remember {
        mutableStateOf(false)
    }
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.playbackState.collectLatest {
            if (it?.isPlayingEnabling == true) {
                if (scaffoldState.bottomSheetState.isVisible.not()) {
                    scaffoldState.bottomSheetState.show()
                }
            } else {
                if (scaffoldState.bottomSheetState.isVisible.not()) {
                    scaffoldState.bottomSheetState.hide()
                }
            }
        }
    }
    BackHandler {
        when {
            scaffoldState.bottomSheetState.currentValue.equals(SheetValue.Expanded) -> {
                scope.launch {
                    scaffoldState.bottomSheetState.hide()
                    scaffoldState.bottomSheetState.show()
                }
            }

            else -> {
                (context as Activity).finish()
            }
        }
    }
    val audioSessionId by viewModel.audioSessionId.collectAsStateWithLifecycle()
//    if (isPlaying) Toast.makeText(
//        context,
//        MusicService.audioSessionId.toString(),
//        Toast.LENGTH_SHORT
//    ).show()
    val isPlayingEnabled by viewModel.isPlayingEnabled.collectAsStateWithLifecycle()
    val duration by viewModel.currentSongDuration.collectAsStateWithLifecycle()
    val progress = viewModel.currentSongPosition.collectAsStateWithLifecycleWithAnimation()
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            scope.launch {
                offsetSearchbar.animateTo(
                    offsetSearchbar.value.plus(available.y).coerceIn(-76f, 12f)
                )
            }
            return super.onPreScroll(available, source)
        }
    }
    val corners = animateDpAsState(
        targetValue = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) 0.dp else 12.dp,
        label = "corners"
    )
    BottomSheetScaffold(
        sheetSwipeEnabled = false,
        sheetContent = {
            if (currentPlayingMusic != null)
                SheetContent(
                    currentPlayingMusic!!.toSong(),
                    context,
                    scaffoldState.bottomSheetState,
                    isPlaying,
                    progress.value,
                    duration,
                    isFavorite,
                    soundLevel = soundLevel,
                    onStop = {
                        viewModel.stop()
                    },
                    onPreviews = {
                        viewModel.skipToPrevious()
                        viewModel.getAudioSessionId()
                    },
                    onNext = {
                        viewModel.skipToNext()
                        viewModel.getAudioSessionId()
                    },
                    onPlayChange = {
                        if (it) viewModel.play() else viewModel.pause()
                    },
                    audioSessionId = audioSessionId,
                    onChangeMode = {
                        viewModel.changeMode(it)
                    },
                    onSoundLevelChanged = {
                        soundLevel = it
                        viewModel.changeSoundLevel(it)
                    },
                    onClose = {
                        scope.launch {
                            scaffoldState.bottomSheetState.hide()
                            scaffoldState.bottomSheetState.show()
                        }
                    },
                    requestFavorite = {
                        if (currentPlayingMusic != null) {
                            viewModel.requestFavorite(
                                currentPlayingMusic!!.toSong()
                            )
                        }
                        if (isFavoriteMode) {
                            viewModel.getFavorites()
                        }
                    },
                    onOpen = {
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    },

                    ) {
                    scope.launch {
                        progress.snapTo(it)
                    }
                    viewModel.seekTo(it.toLong())
                }
        },
        scaffoldState = scaffoldState,
        sheetDragHandle = null,
        sheetPeekHeight = 64.dp,
        modifier = Modifier.fillMaxSize(),
        sheetShape = RoundedCornerShape(topStart = corners.value, topEnd = corners.value)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            SearchBar(
                query = search,
                onQueryChange = viewModel::searchAudios,
                onSearch = { },
                active = isActive,
                onActiveChange = {
                    scope.launch {
                        if (it) {
                            offsetSearchbar.animateTo(0f)
                        } else {
                            offsetSearchbar.animateTo(12f)
                            viewModel.isActive.value = false
                            viewModel.searchAudios("")
                        }
                    }
                    viewModel.isActive.value = it
                },
                modifier = Modifier.offset(y = offsetSearchbar.value.dp),
                placeholder = {
                    Text(text = "Search your music")
                }
            ) {
                ListAudios(
                    nestedScrollConnection = null,
                    songs = songsSearch,
                    context = context,
                    currentPlaying = currentPlayingMusic?.toSong()
                ) {
                    viewModel.playOrToggleButton(it)
                    viewModel.getAudioSessionId()
                }
            }
            ListAudios(
                nestedScrollConnection = nestedScrollConnection,
                songs = songs,
                context = context,
                isFavorite = isFavoriteMode,
                onFavorite = {
                    isFavoriteMode = isFavoriteMode.not()
                    if (isFavoriteMode) viewModel.getFavorites() else viewModel.searchAudios("")
                },
                currentPlaying = currentPlayingMusic?.toSong(),
                onShuffle = { viewModel.shuffledMode() }

            ) {
                viewModel.playOrToggleButton(it)
                viewModel.getAudioSessionId()
            }
        }
    }
    startBroadcast(context = context) {
        if (it.round(1) != soundLevel.round(1)) {
            scope.launch {
                soundLevel = it.round(
                    1
                )
            }
        }
    }
}

@Composable
fun ListAudios(
    nestedScrollConnection: NestedScrollConnection?,
    songs: Resource<List<Song>?>,
    context: Context,
    currentPlaying: Song?,
    isFavorite: Boolean = false,
    onShuffle: (() -> Unit)? = null,
    onFavorite: (() -> Unit)? = null,
    onItemClick: (song: Song) -> Unit,
) {
    val modifier = if (nestedScrollConnection == null) Modifier else Modifier.nestedScroll(
        nestedScrollConnection
    )
    val favoriteColor by animateColorAsState(
        targetValue = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary,
        label = ""
    )
    LazyColumn(
        modifier = modifier
    ) {
        if (nestedScrollConnection != null) {
            item {
                Spacer(modifier = Modifier.height(92.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FloatingActionButton(
                        onClick = { onFavorite?.invoke() },
                        containerColor = favoriteColor
                    ) {
                        Icon(imageVector = Icons.Rounded.FavoriteBorder, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { onShuffle?.invoke() },
                    ) {
                        Icon(
                            painterResource(id = R.drawable.round_shuffle_24),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        if (songs.status == Status.SUCCESS) {
            if (songs.data?.isEmpty() != false) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LottieAnimation(
                            composition = rememberLottieComposition(
                                spec = LottieCompositionSpec.RawRes(
                                    R.raw.empty
                                ),
                            ).value,
                            restartOnPlay = true,
                            isPlaying = true,
                        )
                    }
                }
            }
            items(songs.data!!.size) {
                AudioItem(
                    song = songs.data[it],
                    isSelect = currentPlaying?.mediaId == songs.data[it].mediaId,
                    context = context
                ) {
                    onItemClick.invoke(songs.data[it])
                }
                if (it != songs.data.size.minus(1)) {
                    Divider()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}


@SuppressLint("UnusedCrossfadeTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetContent(
    song: Song,
    context: Context,
    state: SheetState,
    isPlaying: Boolean,
    progress: Float,
    duration: Float,
    isFavorite: Boolean,
    audioSessionId: Int,
    soundLevel: Float,
    onStop: () -> Unit,
    onChangeMode: (Int) -> Unit,
    onOpen: () -> Unit,
    onPreviews: () -> Unit,
    requestFavorite: () -> Unit,
    onSoundLevelChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPlayChange: (Boolean) -> Unit,
    onProgressChanged: (Float) -> Unit,
) {
    Box {
        AnimatedVisibility(
            visible = state.currentValue != SheetValue.Expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SheetMin(
                song.imageUri,
                song.title,
                context,
                isPlaying,
                progress,
                duration,
                onPlayChange,
                onNext,
                onPreviews,
                onStop,
                onOpen
            )
        }
        AnimatedVisibility(
            visible = state.targetValue == SheetValue.Expanded || state.currentValue == SheetValue.Expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SheetMax(
                song,
                context,
                progress,
                duration,
                isFavorite,
                onProgressChanged,
                onPlayChange,
                audioSessionId,
                onNext,
                onChangeMode,
                onPreviews,
                isPlaying,
                soundLevel,
                onSoundLevelChanged,
                onClose,
                requestFavorite
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedCrossfadeTargetStateParameter", "SimpleDateFormat")
@Composable
fun SheetMax(
    song: Song,
    context: Context,
    progressValue: Float,
    durationValue: Float,
    isFavorite: Boolean,
    onProgressChanged: (Float) -> Unit,
    onPlayChange: (Boolean) -> Unit,
    audioSessionId: Int,
    onNext: () -> Unit,
    onChangeMode: (Int) -> Unit,
    onPreviews: () -> Unit,
    isPlaying: Boolean,
    soundLevel: Float,
    onSoundLevelChanged: (Float) -> Unit,
    onClose: () -> Unit,
    requestFavorite: () -> Unit
) {
    val color = MaterialTheme.colorScheme.primary
    var expandMenu by remember {
        mutableStateOf(false)
    }
    var expandModalEqualizer by remember {
        mutableStateOf(false)
    }
    val colorOfMenu by animateColorAsState(
        targetValue = if (expandMenu) MaterialTheme.colorScheme.primary.copy(0.4f) else Color.Transparent,
        label = ""
    )

    val date = remember(song) { SimpleDateFormat("yyyy/MM/dd").format(song.dateAdded) }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Crossfade(targetState = song.imageUri, label = "") {
            AsyncImage(
                model = ImageRequest.Builder(context).data(song.imageUri).error(R.drawable.img)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .alpha(0.6f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f))
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = {
                            expandMenu = true
                        },
                        Modifier.background(
                            colorOfMenu,
                            shape = CircleShape
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = expandMenu, onDismissRequest = { expandMenu = false }) {
                        DropdownMenuItem(text = { Text(text = "equalizer") }, onClick = {
                            expandModalEqualizer = true
                            expandMenu = false
                        })
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            ImageAndSoundForSong(
                image = song.imageUri,
                context = context,
                soundLevel,
                onSoundLevelChanged
            )
            Text(
                text = song.title,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .basicMarquee(
                        initialDelayMillis = 2000,
                        iterations = Int.MAX_VALUE,
                        velocity = 100.dp,
                        delayMillis = 3000,
                        animationMode = MarqueeAnimationMode.Immediately
                    )
                    .padding(top = 18.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = song.subtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .alpha(0.7f),
                style = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = date,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .alpha(0.6f),
                style = TextStyle(
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp), verticalArrangement = Arrangement.Bottom
                ) {
                    AnimatedVisibility(visible = isPlaying, label = "") {
                        AndroidView(factory = {
                            VisualizerView(it, color.toArgb())
                        }, update = {
                            it.listen(audioSessionId)
                        }, onRelease = {
                            it.releaseVisualizer()
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(progressValue),
                            modifier = Modifier,
                        )
                        Text(
                            text = formatTime(durationValue),
                            modifier = Modifier,
                        )
                    }
                    Slider(
                        value = progressValue,
                        onValueChange = onProgressChanged,
                        valueRange = 1f..durationValue,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 18.dp, bottom = 48.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = requestFavorite) {
                            Crossfade(targetState = isFavorite, label = "") {
                                if (isFavorite) {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.FavoriteBorder,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onPreviews) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_skip_previous_24),
                                contentDescription = null
                            )
                        }
                        FloatingActionButton(onClick = { onPlayChange.invoke(isPlaying.not()) }) {
                            Crossfade(targetState = isPlaying, label = "") {
                                if (it) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.round_pause_24),
                                        contentDescription = null
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onNext) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_skip_next_24),
                                contentDescription = null
                            )
                        }
                        RepeatButton(onChangeMode = onChangeMode)
                    }
                    Text(
                        text = "Powered by Amirreza Gholami",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .alpha(0.5f),
                        style = TextStyle(
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        ),
                    )
                }
            }
        }
    }
    if (expandModalEqualizer) {
        EqualizerDialog(context, audioSessionId) {
            expandModalEqualizer = false
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SheetMin(
    image: Uri?,
    text: String,
    context: Context,
    isPlaying: Boolean,
    progress: Float,
    duration: Float,
    onPlayChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onPreviews: () -> Unit,
    onStop: () -> Unit,
    onOpen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onOpen,
                indication = null,
                enabled = true,
                interactionSource = MutableInteractionSource()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).crossfade(true)
                            .placeholder(R.drawable.img).error(R.drawable.img).data(image)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = text,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .fillMaxWidth(0.4f)
                            .focusable()
                            .basicMarquee(
                                initialDelayMillis = 2000,
                                iterations = Int.MAX_VALUE,
                                velocity = 100.dp,
                                delayMillis = 3000,
                                animationMode = MarqueeAnimationMode.Immediately
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onPreviews) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_skip_previous_24),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { onPlayChange.invoke(isPlaying.not()) }) {
                        Crossfade(targetState = isPlaying, label = "") {
                            if (it) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_pause_24),
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    IconButton(onClick = onNext) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_skip_next_24),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onStop) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null
                        )
                    }
                }
            }
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = 1 / duration * progress,
                strokeCap = StrokeCap.Round
            )
        }
    }
}