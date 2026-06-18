package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavHost(viewModel: MainViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    var isSwipingImage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val isDeletingData by viewModel.isDeletingData.collectAsState()
    val isChangingPrompt by viewModel.isChangingPrompt.collectAsState()
    val changePromptStage by viewModel.changePromptStage.collectAsState()
    val deleteAnimationStage by viewModel.deleteAnimationStage.collectAsState()
    val deleteAnimationTriggered by viewModel.deleteAnimationTriggered.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isDeletingData, isChangingPrompt) {
        if (!isDeletingData && !isChangingPrompt && !deleteAnimationTriggered && pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    currentPage = pagerState.currentPage,
                    onPageSelected = { selectedPage ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(selectedPage)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    userScrollEnabled = !isSwipingImage
                ) { page ->
                    when (page) {
                        0 -> TodayScreen(viewModel = viewModel, onSwipeStateChange = { isSwipingImage = it })
                        1 -> DiaryScreen(viewModel = viewModel)
                        2 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isDeletingData || isChangingPrompt,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(1200)),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(1200)),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "reset_transition")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.9f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "reset_alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = if (isDeletingData) deleteAnimationStage else changePromptStage,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) togetherWith
                        androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                    },
                    label = "AnimationMessage"
                ) { stage ->
                    val message = when {
                        isDeletingData && stage == 1 -> "Turning the page."
                        isDeletingData && stage == 2 -> "Ready for new memories."
                        isChangingPrompt && stage == 1 -> "Finding a fresh perspective..."
                        isChangingPrompt && stage == 2 -> "New prompt ready."
                        else -> ""
                    }
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                            modifier = Modifier.padding(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentPage: Int, onPageSelected: (Int) -> Unit) {
    val items = listOf(
        BottomNavItem("Today", 0, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Gallery", 1, Icons.Filled.DateRange, Icons.Outlined.DateRange),
        BottomNavItem("Journey", 2, Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { item ->
            val selected = currentPage == item.page
            NavigationBarItem(
                selected = selected,
                onClick = { onPageSelected(item.page) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val page: Int,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

