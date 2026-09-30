package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.common.CHART_SUPPORTED_COUNTRY
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.data.model.home.chart.Chart
import com.maxrave.domain.data.model.mood.Mood
import com.maxrave.domain.extension.now
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.extension.angledGradientBackground
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.isScrollingUp
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.DropdownButton
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FootgunsStarDialog
import com.maxrave.simpmusic.ui.component.ReviewDialog
import com.maxrave.simpmusic.ui.component.HomeItem
import com.maxrave.simpmusic.ui.component.HomeItemContentPlaylist
import com.maxrave.simpmusic.ui.component.HomeShimmer
import com.maxrave.simpmusic.ui.component.ItemArtistChart
import com.maxrave.simpmusic.ui.component.ListenTogetherIconButton
import com.maxrave.simpmusic.ui.component.MoodMomentAndGenreHomeItem
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.OfflineErrorState
import com.maxrave.simpmusic.ui.component.QuickPicksItem
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.ShareSavedLyricsDialog
import com.maxrave.simpmusic.ui.icon.History
import com.maxrave.simpmusic.ui.icon.Notifications
import com.maxrave.simpmusic.ui.icon.Settings
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.ListenTogetherDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.MoodDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.NotificationDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.RecentlySongsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.desktopPanelDark
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.FOOTGUNS_STAR_KEY
import com.maxrave.simpmusic.viewModel.HomeViewModel
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_COMMUTE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_ENERGIZE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_FEEL_GOOD
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_FOCUS
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_PARTY
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_RELAX
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_ROMANCE
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_SAD
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_SLEEP
import com.maxrave.simpmusic.viewModel.HomeViewModel.Companion.HOME_PARAMS_WORKOUT
import com.maxrave.simpmusic.viewModel.ListState
import com.maxrave.simpmusic.viewModel.SharedViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.all
import simpmusic.composeapp.generated.resources.app_name
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.chart
import simpmusic.composeapp.generated.resources.commute
import simpmusic.composeapp.generated.resources.do_not_show_again
import simpmusic.composeapp.generated.resources.energize
import simpmusic.composeapp.generated.resources.feel_good
import simpmusic.composeapp.generated.resources.focus
import simpmusic.composeapp.generated.resources.go_to_log_in_page
import simpmusic.composeapp.generated.resources.good_afternoon
import simpmusic.composeapp.generated.resources.good_evening
import simpmusic.composeapp.generated.resources.good_morning
import simpmusic.composeapp.generated.resources.good_night
import simpmusic.composeapp.generated.resources.let_s_pick_a_playlist_for_you
import simpmusic.composeapp.generated.resources.let_s_start_with_a_radio
import simpmusic.composeapp.generated.resources.log_in_warning
import simpmusic.composeapp.generated.resources.party
import simpmusic.composeapp.generated.resources.quick_picks
import simpmusic.composeapp.generated.resources.relax
import simpmusic.composeapp.generated.resources.romance
import simpmusic.composeapp.generated.resources.sad
import simpmusic.composeapp.generated.resources.sleep
import simpmusic.composeapp.generated.resources.top_artists
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.what_is_best_choice_today
import simpmusic.composeapp.generated.resources.workout


private val listOfHomeChip =
    listOf(
        Res.string.all, Res.string.relax, Res.string.sleep, Res.string.energize, Res.string.sad,
        Res.string.romance, Res.string.feel_good, Res.string.workout, Res.string.party,
        Res.string.commute, Res.string.focus,
    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onScrolling: (onTop: Boolean) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val isScrollingUp by scrollState.isScrollingUp()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val homeData by viewModel.homeItemList.collectAsStateWithLifecycle()
    val newRelease by viewModel.newRelease.collectAsStateWithLifecycle()
    val chart by viewModel.chart.collectAsStateWithLifecycle()
    val moodMomentAndGenre by viewModel.exploreMoodItem.collectAsStateWithLifecycle()
    val chartLoading by viewModel.loadingChart.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val regionChart by viewModel.regionCodeChart.collectAsStateWithLifecycle()
    val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val chipRowState = rememberScrollState()
    val params by viewModel.params.collectAsStateWithLifecycle()
    val homeListState by viewModel.homeListState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()
    val shouldShowLogInAlert by viewModel.showLogInAlert.collectAsStateWithLifecycle()
    val openAppTime by sharedViewModel.openAppTime.collectAsStateWithLifecycle()
    val shareLyricsPermissions by sharedViewModel.shareSavedLyrics.collectAsStateWithLifecycle()

    val backgroundColor = MaterialTheme.colorScheme.background
    val isLightTheme = backgroundColor.luminance() > 0.5f
    val pageBackground =
        if (getPlatform() == Platform.Desktop) {
            if (isLightTheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        } else backgroundColor
    val heroAccentColor = MaterialTheme.colorScheme.primaryContainer

    var showReviewDialog by rememberSaveable { mutableStateOf(false) }
    var showRequestShareLyricsPermissions by rememberSaveable { mutableStateOf(false) }
    var showFootgunsDialog by rememberSaveable { mutableStateOf(false) }
    var topAppBarHeightPx by rememberSaveable { mutableIntStateOf(0) }
    val hazeState = rememberHazeState(blurEnabled = false)

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }.collect {
            if (it <= 1) onScrolling.invoke(true) else onScrolling.invoke(isScrollingUp)
        }
    }
    val onRefresh: () -> Unit = {
        isRefreshing = true
        viewModel.getHomeItemList(params)
        Logger.w("HomeScreen", "onRefresh")
    }
    LaunchedEffect(key1 = reloadDestination) {
        if (reloadDestination == HomeDestination::class) {
            if (scrollState.firstVisibleItemIndex > 1) {
                scrollState.animateScrollToItem(0)
                sharedViewModel.reloadDestinationDone()
            } else onRefresh.invoke()
        }
    }
    LaunchedEffect(key1 = loading) {
        if (!loading) {
            isRefreshing = false
            sharedViewModel.reloadDestinationDone()
            coroutineScope.launch { pullToRefreshState.animateToHidden() }
        }
    }
    LaunchedEffect(openAppTime, shareLyricsPermissions) {
        if ((openAppTime == 1 || openAppTime % 15 == 0) &&
            openAppTime <= 60 &&
            !shareLyricsPermissions
        ) {
            showRequestShareLyricsPermissions = true
        } else if (openAppTime % 10 == 6 &&
            openAppTime <= 46 &&
            sharedViewModel.getString(FOOTGUNS_STAR_KEY) != "true"
        ) {
            showFootgunsDialog = true
        } else {
            showReviewDialog = false
            showFootgunsDialog = false
            showRequestShareLyricsPermissions = false
        }
    }
    val shouldStartPaginate = remember {
        derivedStateOf {
            homeListState != ListState.PAGINATION_EXHAUST &&
                (scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -9) >=
                (scrollState.layoutInfo.totalItemsCount - 1)
        }
    }
    LaunchedEffect(key1 = shouldStartPaginate.value) {
        if (shouldStartPaginate.value && homeListState == ListState.IDLE) {
            viewModel.getContinueHomeItem(continuation)
        }
    }

    if (showReviewDialog) {
        ReviewDialog(
            onDismissRequest = {
                sharedViewModel.onDoneReview(isDismissOnly = true)
                showReviewDialog = false
            },
            onDoneReview = {
                sharedViewModel.onDoneReview(isDismissOnly = false)
                showReviewDialog = false
            },
        )
    }

    if (showFootgunsDialog) {
        FootgunsStarDialog(
            onDismissRequest = { showFootgunsDialog = false },
            onDoneStar = {
                sharedViewModel.putString(FOOTGUNS_STAR_KEY, "true")
                showFootgunsDialog = false
            },
        )
    }

    if (showRequestShareLyricsPermissions) {
        ShareSavedLyricsDialog(
            onDismissRequest = { showRequestShareLyricsPermissions = false; sharedViewModel.onDoneReview(true) },
            onConfirm = { sharedViewModel.onDoneRequestingShareLyrics(it) },
        )
    }
    if (shouldShowLogInAlert) {
        var doNotShowAgain by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            title = { Text(stringResource(Res.string.warning)) },
            text = {
                Column {
                    Text(stringResource(Res.string.log_in_warning))
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { doNotShowAgain = !doNotShowAgain }.fillMaxWidth(),
                    ) {
                        Checkbox(checked = doNotShowAgain, onCheckedChange = { doNotShowAgain = it })
                        Spacer(Modifier.width(5.dp))
                        Text(stringResource(Res.string.do_not_show_again))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.doneShowLogInAlert(doNotShowAgain); navController.navigate(LoginDestination) }) { Text(stringResource(Res.string.go_to_log_in_page)) } },
            dismissButton = { TextButton(onClick = { viewModel.doneShowLogInAlert(doNotShowAgain) }) { Text(stringResource(Res.string.cancel)) } },
            onDismissRequest = { viewModel.doneShowLogInAlert() },
        )
    }

    Box {
        PullToRefreshBox(
            modifier = Modifier.hazeSource(hazeState),
            state = pullToRefreshState,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = with(LocalDensity.current) { topAppBarHeightPx.toDp() }),
                    containerColor = PullToRefreshDefaults.indicatorContainerColor,
                    color = PullToRefreshDefaults.indicatorColor,
                    maxDistance = PullToRefreshDefaults.PositionalThreshold,
                )
            },
        ) {
            Crossfade(targetState = loading, label = "Home Shimmer") { isLoading ->
                if (!isLoading) {
                    if (homeData.isEmpty()) {
                        OfflineErrorState(onRetry = onRefresh, onOpenDownloaded = { navController.navigate(LibraryDynamicPlaylistDestination(type = LibraryDynamicPlaylistType.Downloaded.toStringParams())) })
                        return@Crossfade
                    }
                    LazyColumn(state = scrollState, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        itemsIndexed(homeData, key = { _, item -> item.hashCode().toString() }) { index, item ->
                            Box {
                                if (index == 0) {
                                    Box(Modifier.matchParentSize().angledGradientBackground(listOf(heroAccentColor, pageBackground), 25f)) {
                                        Box(Modifier.fillMaxWidth().height(150.dp).align(Alignment.BottomCenter).background(artworkScrimBrush(pageBackground)))
                                    }
                                }
                                Column(Modifier.padding(horizontal = 18.dp)) {
                                    if (index == 0) Spacer(Modifier.height(with(LocalDensity.current) { topAppBarHeightPx.toDp() }))
                                    Spacer(Modifier.height(6.dp))
                                    if (item.title == stringResource(Res.string.quick_picks)) {
                                        AnimatedVisibility(visible = true) {
                                            QuickPicks(
                                                homeItem = item.copy(contents = item.contents.mapNotNull { ct -> ct?.copy(artists = ct.artists?.let { art -> if (art.size > 1) art.dropLast(1) else art }) }),
                                                navController = navController,
                                                viewModel = viewModel,
                                            )
                                        }
                                    } else HomeItem(navController = navController, data = item)
                                }
                            }
                        }
                        item {
                            AnimatedVisibility(homeListState == ListState.PAGINATING, enter = fadeIn(tween(120)), exit = androidx.compose.animation.ExitTransition.None) {
                                CenterLoadingBox(Modifier.fillMaxWidth().height(200.dp))
                            }
                        }
                        if (homeListState == ListState.PAGINATION_EXHAUST) {
                            items(newRelease, key = { "release_${it.title}_${it.channelId ?: it.subtitle ?: ""}" }) { Box(Modifier.padding(horizontal = 18.dp)) { HomeItem(navController = navController, data = it) } }
                            item { moodMomentAndGenre?.let { Box(Modifier.padding(horizontal = 18.dp)) { MoodMomentAndGenre(it, navController) } } }
                            item {
                                Column(Modifier.padding(vertical = 10.dp).padding(horizontal = 18.dp)) {
                                    ChartTitle()
                                    Spacer(Modifier.height(5.dp))
                                    regionChart?.let { region ->
                                        DropdownButton(
                                            items = CHART_SUPPORTED_COUNTRY.itemsData.toList(),
                                            defaultSelected = CHART_SUPPORTED_COUNTRY.itemsData.getOrNull(CHART_SUPPORTED_COUNTRY.items.indexOf(region)) ?: CHART_SUPPORTED_COUNTRY.itemsData[1],
                                        ) { selected -> viewModel.exploreChart(CHART_SUPPORTED_COUNTRY.items[CHART_SUPPORTED_COUNTRY.itemsData.indexOf(selected)]) }
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    if (!chartLoading) chart?.let { ChartData(it, navController) } else CenterLoadingBox(Modifier.fillMaxWidth().height(400.dp))
                                }
                            }
                        }
                        item { EndOfPage() }
                    }
                } else Column { Spacer(Modifier.height(with(LocalDensity.current) { topAppBarHeightPx.toDp() })); HomeShimmer() }
            }
        }
        AnimatedContent(
            targetState = scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0,
            transitionSpec = { fadeIn(tween(0)).togetherWith(fadeOut(tween(0))) },
        ) { target ->
            Column(
                modifier = Modifier.align(Alignment.TopCenter).then(
                    if (target) Modifier.background(Color.Transparent)
                    else Modifier.hazeEffect(hazeState, style = HazeMaterials.ultraThin()) { blurEnabled = false },
                ).onGloballyPositioned { topAppBarHeightPx = it.size.height },
            ) {
                AnimatedVisibility(visible = isScrollingUp, enter = androidx.compose.animation.EnterTransition.None, exit = androidx.compose.animation.ExitTransition.None) {
                    HomeTopAppBar(navController, accountInfo?.first)
                }
                AnimatedVisibility(visible = !isScrollingUp, enter = androidx.compose.animation.EnterTransition.None, exit = androidx.compose.animation.ExitTransition.None) {
                    Spacer(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars))
                }
                Row(
                    Modifier.horizontalScroll(chipRowState).padding(vertical = 8.dp, horizontal = 18.dp).background(Color.Transparent),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOfHomeChip.forEach { id ->
                        val isSelected = when (params) {
                            HOME_PARAMS_RELAX -> id == Res.string.relax; HOME_PARAMS_SLEEP -> id == Res.string.sleep
                            HOME_PARAMS_ENERGIZE -> id == Res.string.energize; HOME_PARAMS_SAD -> id == Res.string.sad
                            HOME_PARAMS_ROMANCE -> id == Res.string.romance; HOME_PARAMS_FEEL_GOOD -> id == Res.string.feel_good
                            HOME_PARAMS_WORKOUT -> id == Res.string.workout; HOME_PARAMS_PARTY -> id == Res.string.party
                            HOME_PARAMS_COMMUTE -> id == Res.string.commute; HOME_PARAMS_FOCUS -> id == Res.string.focus
                            else -> id == Res.string.all
                        }
                        Chip(isAnimated = loading, isSelected = isSelected, text = stringResource(id)) {
                            when (id) {
                                Res.string.all -> viewModel.setParams(null); Res.string.relax -> viewModel.setParams(HOME_PARAMS_RELAX)
                                Res.string.sleep -> viewModel.setParams(HOME_PARAMS_SLEEP); Res.string.energize -> viewModel.setParams(HOME_PARAMS_ENERGIZE)
                                Res.string.sad -> viewModel.setParams(HOME_PARAMS_SAD); Res.string.romance -> viewModel.setParams(HOME_PARAMS_ROMANCE)
                                Res.string.feel_good -> viewModel.setParams(HOME_PARAMS_FEEL_GOOD); Res.string.workout -> viewModel.setParams(HOME_PARAMS_WORKOUT)
                                Res.string.party -> viewModel.setParams(HOME_PARAMS_PARTY); Res.string.commute -> viewModel.setParams(HOME_PARAMS_COMMUTE)
                                Res.string.focus -> viewModel.setParams(HOME_PARAMS_FOCUS)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(navController: NavController, accountName: String? = null) {
    val hour = remember { now().time.hour }
    val greeting = when (hour) {
        in 6..12 -> stringResource(Res.string.good_morning)
        in 13..17 -> stringResource(Res.string.good_afternoon)
        in 18..23 -> stringResource(Res.string.good_evening)
        else -> stringResource(Res.string.good_night)
    }
    TopAppBar(
        windowInsets = TopAppBarDefaults.windowInsets.exclude(TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start)),
        title = {
            Column {
                Text(
                    stringResource(Res.string.app_name),
                    style = typo().headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    if (accountName.isNullOrBlank()) greeting else "$greeting, $accountName",
                    style = typo().bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        },
        actions = {
            RippleIconButton(imageVector = SimpIcons.Notifications, tint = MaterialTheme.colorScheme.onBackground) { navController.navigate(NotificationDestination) }
            RippleIconButton(imageVector = SimpIcons.History, tint = MaterialTheme.colorScheme.onBackground) { navController.navigate(RecentlySongsDestination) }
            ListenTogetherIconButton { navController.navigate(ListenTogetherDestination) }
            RippleIconButton(imageVector = SimpIcons.Settings, tint = MaterialTheme.colorScheme.onBackground) { navController.navigate(SettingsDestination) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun PgSectionTitle(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = text,
            style = typo().headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
    }
}

@ExperimentalFoundationApi
@Composable
fun QuickPicks(homeItem: HomeItem, navController: NavController, viewModel: HomeViewModel = koinViewModel()) {
    val lazyListState = rememberLazyGridState()
    val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState, snapPosition = SnapPosition.Start))
    val density = LocalDensity.current
    var widthDp by remember { mutableStateOf(0.dp) }
    var bottomSheetShow by remember { mutableStateOf(false) }
    var track by remember { mutableStateOf<Track?>(null) }
    if (bottomSheetShow) NowPlayingBottomSheet(onDismiss = { bottomSheetShow = false }, song = track?.toSongEntity(), navController = navController)
    Column(Modifier.padding(vertical = 6.dp).onGloballyPositioned { with(density) { widthDp = it.size.width.toDp() } }) {
        Text(stringResource(Res.string.let_s_start_with_a_radio), style = typo().bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PgSectionTitle(stringResource(Res.string.quick_picks))
        LazyHorizontalGrid(rows = GridCells.Fixed(4), modifier = Modifier.height(256.dp), state = lazyListState, flingBehavior = snapperFlingBehavior) {
            items(homeItem.contents, key = { item -> item?.videoId ?: item?.browseId ?: item?.playlistId ?: item?.title ?: item.hashCode() }) { item ->
                item?.let {
                    QuickPicksItem(
                        onClick = {
                            val firstQueue = it.toTrack()
                            viewModel.setQueueData(QueueData.Data(arrayListOf(firstQueue), firstQueue, "RDAMVM${it.videoId}", "\"${it.title}\" Radio", PlaylistType.RADIO, null))
                            viewModel.loadMediaItem(firstQueue, Config.SONG_CLICK)
                        },
                        onLongClick = { track = it.toTrack(); bottomSheetShow = true }, data = it, widthDp = widthDp,
                    )
                }
            }
        }
    }
}

@Composable
fun MoodMomentAndGenre(mood: Mood, navController: NavController) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(stringResource(Res.string.let_s_pick_a_playlist_for_you), style = typo().bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        mood.sections.forEach { section ->
            val gridState = rememberLazyGridState()
            val flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = gridState))
            PgSectionTitle(section.title)
            LazyHorizontalGrid(rows = GridCells.Fixed(3), modifier = Modifier.height(210.dp), state = gridState, flingBehavior = flingBehavior) {
                items(section.items, key = { it.params }) { item -> MoodMomentAndGenreHomeItem(item.title, item.stripeColor) { navController.navigate(MoodDestination(item.params)) } }
            }
        }
    }
}

@Composable
fun ChartTitle() {
    Column {
        Text(stringResource(Res.string.what_is_best_choice_today), style = typo().bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PgSectionTitle(stringResource(Res.string.chart))
    }
}

@Composable
fun ChartData(chart: Chart, navController: NavController) {
    var gridWidthDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val lazyListState2 = rememberLazyGridState()
    val snapperFlingBehavior2 = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyGridState = lazyListState2))
    Column(Modifier.onGloballyPositioned { with(density) { gridWidthDp = it.size.width.toDp() } }) {
        chart.listChartItem.forEach { item ->
            PgSectionTitle(item.title)
            val lazyListState = rememberLazyListState()
            val snapperFlingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(lazyListState = lazyListState))
            LazyRow(flingBehavior = snapperFlingBehavior) {
                items(item.playlists.size, key = { index -> val data = item.playlists[index]; data.id + data.title + index }) { index ->
                    HomeItemContentPlaylist(onClick = { navController.navigate(PlaylistDestination(item.playlists[index].id, false)) }, data = item.playlists[index])
                }
            }
        }
        PgSectionTitle(stringResource(Res.string.top_artists))
        LazyHorizontalGrid(rows = GridCells.Fixed(3), modifier = Modifier.height(240.dp), state = lazyListState2, flingBehavior = snapperFlingBehavior2) {
            items(chart.artists.itemArtists.size, key = { index -> val item = chart.artists.itemArtists[index]; item.title + item.browseId + index }) { index ->
                val data = chart.artists.itemArtists[index]
                ItemArtistChart(onClick = { navController.navigate(ArtistDestination(data.browseId)) }, data = data, widthDp = gridWidthDp)
            }
        }
    }
}
