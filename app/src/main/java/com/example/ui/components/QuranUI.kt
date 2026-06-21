package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.example.ui.viewmodel.PrayerTimesState
import com.example.data.database.BookmarkEntity
import com.example.data.database.VerseEntity
import com.example.data.model.Surah
import com.example.data.model.SurahMetadata
import com.example.ui.theme.GoldBronze
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.QuranViewModel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.center
import androidx.compose.foundation.Canvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAppContent(viewModel: QuranViewModel) {
    val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsStateWithLifecycle()
    val totalCachedCount by viewModel.totalCachedCount.collectAsStateWithLifecycle()
    
    var currentTab by remember { mutableIntStateOf(0) } // 0: Index, 1: Bookmarks, 2: Sync

    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()

    var showWelcomeDialog by rememberSaveable { mutableStateOf(true) }
    if (showWelcomeDialog) {
        QuranWelcomeDialog(onDismiss = { showWelcomeDialog = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                when (currentTheme) {
                    AppThemeMode.DARK -> {
                        // Deep obsidian dark background #0A0C0A
                        drawRect(color = Color(0xFF0A0C0A))
                        
                        // Top-Left emerald glass glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF064E3B).copy(alpha = 0.40f), Color.Transparent),
                                center = Offset(0f, 0f),
                                radius = size.width * 0.9f
                            ),
                            radius = size.width * 0.9f,
                            center = Offset(0f, 0f)
                        )
                        
                        // Bottom-Right emerald glass glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF047857).copy(alpha = 0.20f), Color.Transparent),
                                center = Offset(size.width, size.height),
                                radius = size.width * 1.1f
                            ),
                            radius = size.width * 1.1f,
                            center = Offset(size.width, size.height)
                        )
                    }
                    AppThemeMode.LIGHT -> {
                        // Soft translucent pearl background with light green glows
                        drawRect(color = Color(0xFFF0F4F1))
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFD1FAE5).copy(alpha = 0.55f), Color.Transparent),
                                center = Offset(0f, 0f),
                                radius = size.width * 0.9f
                            ),
                            radius = size.width * 0.9f,
                            center = Offset(0f, 0f)
                        )
                    }
                    AppThemeMode.SEPIA -> {
                        // Soft warm creamy sepia background
                        drawRect(color = Color(0xFFFBF0D9))
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFEBDCBD).copy(alpha = 0.6f), Color.Transparent),
                                center = Offset(0f, 0f),
                                radius = size.width * 1.0f
                            ),
                            radius = size.width * 1.0f,
                            center = Offset(0f, 0f)
                        )
                    }
                }
            }
    ) {
        val appBarBgColor = when (currentTheme) {
            AppThemeMode.DARK -> Color(0xFF0A0C0A).copy(alpha = 0.75f)
            AppThemeMode.LIGHT -> Color(0xFFF0F4F1).copy(alpha = 0.75f)
            AppThemeMode.SEPIA -> Color(0xFFFBF0D9).copy(alpha = 0.75f)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (selectedSurah != null) selectedSurah!!.englishName else "Al-Quran",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (selectedSurah != null) {
                                Text(
                                    text = "${selectedSurah!!.englishNameTranslation} • ${selectedSurah!!.numberOfAyahs} Verses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Offline Study & Recitation Hub",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (selectedSurah != null) {
                            IconButton(
                                onClick = { viewModel.selectSurah(null) },
                                modifier = Modifier.testTag("back_button")
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Index")
                            }
                        } else {
                            Box(modifier = Modifier.padding(start = 12.dp)){
                                Icon(Icons.Default.Menu, contentDescription = "Al-Quran Logo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    actions = {
                        if (selectedSurah == null) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Text("$totalCachedCount Cached", modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = appBarBgColor,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (selectedSurah == null) {
                    val isThemeDark = currentTheme == AppThemeMode.DARK
                    val navBgColor = if (isThemeDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.85f)
                    val navBorderColor = if (isThemeDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                    
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            color = navBgColor,
                            border = BorderStroke(1.dp, navBorderColor),
                            shape = RoundedCornerShape(32.dp),
                            tonalElevation = 0.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(64.dp)
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Index") },
                                    label = { Text("Chapters") },
                                    modifier = Modifier.testTag("nav_chapters_tab")
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Bookmarks") },
                                    label = { Text("Bookmarks") },
                                    modifier = Modifier.testTag("nav_bookmarks_tab")
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Cloud Sync") },
                                    label = { Text("Cloud Sync") },
                                    modifier = Modifier.testTag("nav_sync_tab")
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            AnimatedContent(
                targetState = selectedSurah != null,
                label = "ScreenTransition"
            ) { isReadingMode ->
                if (isReadingMode) {
                    selectedSurah?.let { surah ->
                        SurahReaderScreen(viewModel = viewModel, surah = surah)
                    }
                } else {
                    when (currentTab) {
                        0 -> QuranIndexTab(viewModel = viewModel)
                        1 -> BookmarksTab(viewModel = viewModel, bookmarkedVerses = bookmarkedVerses)
                        2 -> CloudSyncTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
}


@Composable
fun QuranIndexTab(viewModel: QuranViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredSurahs by viewModel.filteredSurahs.collectAsStateWithLifecycle()
    val progressMap by viewModel.surahProgress.collectAsStateWithLifecycle()
    val arabicSize by viewModel.arabicFontSize.collectAsStateWithLifecycle()
    val englishSize by viewModel.englishFontSize.collectAsStateWithLifecycle()
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    
    val isDarkTheme = isSystemInDarkTheme()
    val searchBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.65f)
    val searchBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    var headerTabSelected by remember { mutableStateOf(0) }
    var showFontControls by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Daily prayer times vs Qibla compass switcher card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val options = listOf("Daily Prayer Times", "Qibla Compass")
            options.forEachIndexed { index, text ->
                val isSelected = headerTabSelected == index
                val itemBg = if (isSelected) {
                    if (isDarkTheme) Color(0xFF047857) else Color(0xFFD1FAE5)
                } else {
                    Color.Transparent
                }
                val textColor = if (isSelected) {
                    if (isDarkTheme) Color.White else Color(0xFF065F46)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .clickable { headerTabSelected = index }
                        .padding(vertical = 8.dp)
                        .testTag("dashboard_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (headerTabSelected == 0) {
            PrayerTimesCard(viewModel = viewModel)
        } else {
            QiblaCompassCard(viewModel = viewModel)
        }

        TextField(
            value = searchQuery,
            onValueChange = { viewModel.setSurahSearchQuery(it) },
            placeholder = { Text("Search chapter, verse or word...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSurahSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, searchBorderColor, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .testTag("quran_search_field"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = searchBgColor,
                unfocusedContainerColor = searchBgColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Preferences",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Display & Theme Settings",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = { showFontControls = !showFontControls },
                modifier = Modifier.testTag("toggle_font_controls_index")
            ) {
                Icon(
                    imageVector = if (showFontControls) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle font size sliders"
                )
            }
        }

        AnimatedVisibility(visible = showFontControls) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
                ),
                border = BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Reader Theme Mode",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeSelectorRow(
                        currentTheme = currentTheme,
                        onThemeSelected = { viewModel.updateThemeMode(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(modifier = Modifier.padding(bottom = 8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Arabic Names: ",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = arabicSize,
                            onValueChange = { viewModel.updateArabicSize(it) },
                            valueRange = 24f..48f,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("arabic_font_slider_index")
                        )
                        Text(
                            text = "${arabicSize.toInt()}sp",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "English Names: ",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = englishSize,
                            onValueChange = { viewModel.updateEnglishSize(it) },
                            valueRange = 12f..28f,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("english_font_slider_index")
                        )
                        Text(
                            text = "${englishSize.toInt()}sp",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        if (filteredSurahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "No Surahs Found",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No chapters match your search query", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val progress = progressMap[surah.number] ?: 0
                    SurahCard(
                        surah = surah,
                        progress = progress,
                        arabicTextSize = arabicSize,
                        englishTextSize = englishSize,
                        onClick = { viewModel.selectSurah(surah) }
                    )
                }
                item {
                    QuranDevFooter()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahCard(
    surah: Surah,
    progress: Int = 0,
    arabicTextSize: Float = 32f,
    englishTextSize: Float = 16f,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val cardBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.8f)
    val cardBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("surah_card_${surah.number}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = if (progress > 0) 8.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surah.englishName,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (englishTextSize * 1.05f).sp
                        )
                    )
                    Text(
                        text = "${surah.revelationType} • ${surah.numberOfAyahs} Ayahs",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (englishTextSize * 0.75f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = surah.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (arabicTextSize * 0.65f).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = surah.englishNameTranslation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (englishTextSize * 0.75f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (progress > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reading Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$progress%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SurahReaderScreen(viewModel: QuranViewModel, surah: Surah) {
    val verses by viewModel.versesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingSurah.collectAsStateWithLifecycle()
    val surahError by viewModel.surahError.collectAsStateWithLifecycle()
    
    val playingVerseId by viewModel.playingVerseId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    
    val arabicSize by viewModel.arabicFontSize.collectAsStateWithLifecycle()
    val englishSize by viewModel.englishFontSize.collectAsStateWithLifecycle()
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentLang by viewModel.translationLanguage.collectAsStateWithLifecycle()

    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsStateWithLifecycle()
    
    val bookmarkedIds = remember(bookmarkedVerses) {
        bookmarkedVerses.map { it.id }.toSet()
    }

    val listState = rememberLazyListState()

    val isAtEnd = remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, isAtEnd.value, verses) {
        if (verses.isNotEmpty()) {
            if (isAtEnd.value) {
                viewModel.saveSurahProgress(surah.number, 100)
            } else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisible > 0) {
                    val progressPercent = if (lastVisible >= verses.size) {
                        100
                    } else {
                        ((lastVisible.toFloat() / verses.size.toFloat()) * 100).toInt()
                    }
                    if (progressPercent > 0) {
                        viewModel.saveSurahProgress(surah.number, progressPercent)
                    }
                }
            }
        }
    }

    var showFontControls by remember { mutableStateOf(false) }
    var selectedVerseForTafsir by remember { mutableStateOf<VerseEntity?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val controlCardBg = if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.8f)
    val controlCardBorder = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = controlCardBg),
            border = BorderStroke(1.dp, controlCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = "Preferences", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Display & Theme Settings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                IconButton(onClick = { showFontControls = !showFontControls }) {
                    Icon(
                        imageVector = if (showFontControls) Icons.Default.Settings else Icons.Default.List,
                        contentDescription = "Toggle text and theme options"
                    )
                }
            }

            AnimatedVisibility(visible = showFontControls) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Reader Theme Mode",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemeSelectorRow(
                        currentTheme = currentTheme,
                        onThemeSelected = { viewModel.updateThemeMode(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Translation Language",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TranslationSelectorRow(
                        currentLang = currentLang,
                        onLangSelected = { viewModel.updateTranslationLanguage(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Arabic Text: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(110.dp))
                        Slider(
                            value = arabicSize,
                            onValueChange = { viewModel.updateArabicSize(it) },
                            valueRange = 24f..48f,
                            modifier = Modifier.weight(1f).testTag("arabic_font_slider")
                        )
                        Text("${arabicSize.toInt()}sp", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("English Text: ", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(110.dp))
                        Slider(
                            value = englishSize,
                            onValueChange = { viewModel.updateEnglishSize(it) },
                            valueRange = 12f..28f,
                            modifier = Modifier.weight(1f).testTag("english_font_slider")
                        )
                        Text("${englishSize.toInt()}sp", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        if (isLoading && verses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Downloading verses and caching offline...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (surahError != null && verses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Offline Error", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(surahError!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectSurah(surah) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry Connection")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SurahInformationBanner(
                        surah = surah,
                        arabicTextSize = arabicSize,
                        englishTextSize = englishSize
                    )
                }

                items(verses, key = { it.id }) { verse ->
                    val isPlayingThis = playingVerseId == verse.id
                    val isBookmarked = bookmarkedIds.contains(verse.id)

                    VerseItemCard(
                        verse = verse,
                        currentLang = currentLang,
                        isCurrentlyPlaying = isPlayingThis,
                        isPlayingOverall = isPlaying,
                        isBookmarked = isBookmarked,
                        arabicTextSize = arabicSize,
                        englishTextSize = englishSize,
                        onPlayClick = { viewModel.playRecitation(verse) },
                        onBookmarkClick = { viewModel.toggleBookmark(verse.surahNumber, verse.verseNumber) },
                        onTafsirClick = { selectedVerseForTafsir = verse }
                    )
                }
            }
        }

        if (selectedVerseForTafsir != null) {
            val tafsirState = viewModel.getTafsirForVerse(
                selectedVerseForTafsir!!.surahNumber,
                selectedVerseForTafsir!!.verseNumber
            ).collectAsState(initial = null)

            TafsirDetailDialog(
                verse = selectedVerseForTafsir!!,
                currentLang = currentLang,
                tafsirText = tafsirState.value?.tafsirText,
                onDismiss = { selectedVerseForTafsir = null }
            )
        }

        SurahAudioPlayerBar(
            viewModel = viewModel,
            surah = surah
        )
    }
}

@Composable
fun SurahInformationBanner(
    surah: Surah,
    arabicTextSize: Float = 32f,
    englishTextSize: Float = 16f
) {
    val isDarkTheme = isSystemInDarkTheme()
    val bannerBgColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    val bannerBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = bannerBgColor
        ),
        border = BorderStroke(1.dp, bannerBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = (arabicTextSize * 1.1f).sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${surah.englishName} • ${surah.englishNameTranslation}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (englishTextSize * 1.15f).sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Revealed in ${surah.revelationType} • ${surah.numberOfAyahs} Complete Verses",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (englishTextSize * 0.8f).sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            if (surah.number != 9) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "In the name of Allah, the Entirely Merciful, the Especially Merciful",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun VerseItemCard(
    verse: VerseEntity,
    currentLang: com.example.ui.viewmodel.TranslationLanguage = com.example.ui.viewmodel.TranslationLanguage.ENGLISH,
    isCurrentlyPlaying: Boolean,
    isPlayingOverall: Boolean,
    isBookmarked: Boolean,
    arabicTextSize: Float,
    englishTextSize: Float,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onTafsirClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    val containerColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.8f)
    }

    val border = if (isCurrentlyPlaying) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verse_item_card_${verse.surahNumber}_${verse.verseNumber}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${verse.surahNumber}:${verse.verseNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (isCurrentlyPlaying && isPlayingOverall) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = GoldBronze,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reciting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldBronze
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier
                            .testTag("play_verse_${verse.surahNumber}_${verse.verseNumber}")
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyPlaying && isPlayingOverall) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Play Verse Recitation",
                            tint = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onTafsirClick,
                        modifier = Modifier
                            .testTag("tafsir_button_${verse.surahNumber}_${verse.verseNumber}")
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Study Tafsir Exegesis",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier
                            .testTag("bookmark_verse_${verse.surahNumber}_${verse.verseNumber}")
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldBronze else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = verse.textArabic,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = arabicTextSize.sp,
                lineHeight = (arabicTextSize * 1.5).sp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("arabic_script_${verse.surahNumber}_${verse.verseNumber}")
            )

            val translationText = when (currentLang) {
                com.example.ui.viewmodel.TranslationLanguage.ENGLISH -> verse.textEnglish
                com.example.ui.viewmodel.TranslationLanguage.URDU -> verse.textUrdu.ifEmpty { "Urdu translation not cached. Please make sure you are online and reload/re-download the Surah." }
            }

            Text(
                text = translationText,
                fontSize = englishTextSize.sp,
                lineHeight = if (currentLang == com.example.ui.viewmodel.TranslationLanguage.URDU) (englishTextSize * 1.6).sp else (englishTextSize * 1.4).sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("translation_script_${verse.surahNumber}_${verse.verseNumber}")
            )
        }
    }
}

@Composable
fun BookmarksTab(viewModel: QuranViewModel, bookmarkedVerses: List<BookmarkEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Bookmarks",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tap on any bookmark to jump directly into the chapter reader.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (bookmarkedVerses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Empty bookmarks",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Bookmarks Saved Yet",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Click the star icon on any verse card to save it.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            QuranDevFooter()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(bookmarkedVerses, key = { it.id }) { bookmark ->
                    val surah = SurahMetadata.list.firstOrNull { it.number == bookmark.surahNumber }
                    
                    val isDarkTheme = isSystemInDarkTheme()
                    val cardBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.8f)
                    val cardBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectSurah(surah)
                            }
                            .testTag("bookmark_card_${bookmark.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, cardBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "Bookmark Logo", tint = GoldBronze)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "${surah?.englishName ?: "Surah"} - Verse ${bookmark.verseNumber}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = surah?.englishNameTranslation ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleBookmark(bookmark.surahNumber, bookmark.verseNumber) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Bookmark", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                item {
                    QuranDevFooter()
                }
            }
        }
    }
}

@Composable
fun CloudSyncTab(viewModel: QuranViewModel) {
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncSuccess by viewModel.syncSuccess.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val bookmarkedVerses by viewModel.bookmarkedVerses.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Cloud Synchronization Center",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Keep your study progress, bookmarks, and font scales synchronized seamlessly across all your personal devices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val isDarkTheme = isSystemInDarkTheme()
        val cardBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.8f)
        val cardBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(1.dp, cardBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Active Sync Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("ramybek31@gmail.com", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("Verified Developer Sandbox Profile", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Device Name:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(syncState?.deviceName ?: "This Device (Emulator)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Synced Bookmarks:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${bookmarkedVerses.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Last Synced Cloud Backup:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val lastSyncTime = syncState?.lastSyncedTimestamp
                    val label = if (lastSyncTime != null) {
                        java.text.DateFormat.getDateTimeInstance().format(lastSyncTime)
                    } else {
                        "Never Backed Up"
                    }
                    Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (lastSyncTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sync Cycle Index:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${syncState?.syncCount ?: 0} Uploads", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSyncing) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Connecting to sync cluster... uploading JSON blocks", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Button(
                onClick = { viewModel.syncBackup("ramybek31@gmail.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("sync_backup_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = if (isDarkTheme) Color.Black else Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = "Upload Backup")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back Up & Sync Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        syncSuccess?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        syncError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        QuranDevFooter()
    }
}

@Composable
fun TafsirDetailDialog(
    verse: VerseEntity,
    currentLang: com.example.ui.viewmodel.TranslationLanguage = com.example.ui.viewmodel.TranslationLanguage.ENGLISH,
    tafsirText: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("tafsir_close_button")
            ) {
                Text(
                    "Close Study",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tafsir Mode",
                    tint = GoldBronze,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Verse Exegesis (Tafsir)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Chapter ${verse.surahNumber}, Verse ${verse.verseNumber}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arabic Script Reference
                Text(
                    text = verse.textArabic,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Translation Reference
                val translationText = when (currentLang) {
                    com.example.ui.viewmodel.TranslationLanguage.ENGLISH -> verse.textEnglish
                    com.example.ui.viewmodel.TranslationLanguage.URDU -> verse.textUrdu.ifEmpty { "Urdu translation loading or not cached for this verse." }
                }

                Text(
                    text = translationText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "COMMENTARY & ANALYSIS",
                    letterSpacing = 1.1.sp,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldBronze
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (tafsirText == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Text(
                        text = tafsirText,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tafsir_text_content")
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrayerTimesCard(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            try {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchPrayerTimesForLocation(
                            lat = location.latitude,
                            lng = location.longitude,
                            city = "My Location",
                            country = ""
                        )
                    } else {
                        viewModel.loadPrayerTimesWithFallback()
                    }
                }
            } catch (e: SecurityException) {
                viewModel.loadPrayerTimesWithFallback()
            }
        }
    }

    val state by viewModel.prayerTimesState.collectAsStateWithLifecycle()

    val cardBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF064E3B).copy(alpha = 0.25f),
                Color(0xFF047857).copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFD1FAE5).copy(alpha = 0.70f),
                Color(0xFFE6F4EA).copy(alpha = 0.40f)
            )
        )
    }

    val borderColor = if (isDark) Color(0xFF059669).copy(alpha = 0.25f) else Color(0xFFA7F3D0).copy(alpha = 0.6f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("prayer_times_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(16.dp)
        ) {
            when (val st = state) {
                is PrayerTimesState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Calculating Prayer Times...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is PrayerTimesState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Could not fetch prayer times",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = st.message,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.loadPrayerTimesWithFallback() },
                            modifier = Modifier.testTag("prayer_retry_button")
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is PrayerTimesState.Success -> {
                    val nextPrayer = getNextPrayerName(st.timings)
                    
                    Column {
                        // Location info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = GoldBronze,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${st.city}${if(st.country.isNotEmpty()) ", " + st.country else ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${st.dateReadable} • ${st.hijriDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Refresh / Geolocation Request button
                            IconButton(
                                onClick = {
                                    if (locationPermissionsState.allPermissionsGranted) {
                                        try {
                                            locationClient.lastLocation.addOnSuccessListener { loc ->
                                                if (loc != null) {
                                                    viewModel.fetchPrayerTimesForLocation(
                                                        lat = loc.latitude,
                                                        lng = loc.longitude,
                                                        city = "My Location",
                                                        country = ""
                                                    )
                                                } else {
                                                    viewModel.loadPrayerTimesWithFallback()
                                                }
                                            }
                                        } catch (se: SecurityException) {
                                            viewModel.loadPrayerTimesWithFallback()
                                        }
                                    } else {
                                        locationPermissionsState.launchMultiplePermissionRequest()
                                    }
                                },
                                modifier = Modifier
                                    .testTag("prayer_gps_refresh")
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (locationPermissionsState.allPermissionsGranted) Icons.Default.Refresh else Icons.Default.LocationOn,
                                    contentDescription = "Localize or Refresh Times",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Horizontally scrollable timings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val prayers = listOf(
                                Triple("Fajr", st.timings.Fajr, "الفجر"),
                                Triple("Sunrise", st.timings.Sunrise, "الشروق"),
                                Triple("Dhuhr", st.timings.Dhuhr, "الظهر"),
                                Triple("Asr", st.timings.Asr, "العصر"),
                                Triple("Maghrib", st.timings.Maghrib, "المغرب"),
                                Triple("Isha", st.timings.Isha, "العشاء")
                            )

                            prayers.forEach { (name, time, arabic) ->
                                val isNext = name == nextPrayer
                                val focusBg = if (isDark) {
                                    if (isNext) Color(0xFF047857).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.03f)
                                } else {
                                    if (isNext) Color(0xFF34D399).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.75f)
                                }
                                val bBorder = if (isNext) BorderStroke(1.dp, GoldBronze) else BorderStroke(1.dp, Color.Transparent)

                                Surface(
                                    modifier = Modifier
                                        .width(76.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .testTag("prayer_item_$name"),
                                    color = focusBg,
                                    border = bBorder,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (isNext) {
                                            Text(
                                                text = "NEXT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldBronze,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        } else {
                                            Text(
                                                text = arabic,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isNext) GoldBronze else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatTo12Hour(time),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseTimeToMinutes(timeStr: String): Int {
    return try {
        val cleanTime = timeStr.split(" ")[0]
        val parts = cleanTime.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        h * 60 + m
    } catch (e: Exception) {
        -1
    }
}

fun getNextPrayerName(timings: com.example.data.network.PrayerTimings): String {
    val prayerList = listOf(
        "Fajr" to timings.Fajr,
        "Sunrise" to timings.Sunrise,
        "Dhuhr" to timings.Dhuhr,
        "Asr" to timings.Asr,
        "Maghrib" to timings.Maghrib,
        "Isha" to timings.Isha
    )
    val calendar = java.util.Calendar.getInstance()
    val currMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)
    
    for ((name, time) in prayerList) {
        val pMin = parseTimeToMinutes(time)
        if (pMin > currMinutes) {
            return name
        }
    }
    return "Fajr"
}

fun formatTo12Hour(time24: String): String {
    return try {
        val cleanTime = time24.split(" ")[0]
        val parts = cleanTime.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1]
        val suffix = if (hour >= 12) "PM" else "AM"
        hour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$hour:$minute $suffix"
    } catch (e: Exception) {
        time24
    }
}


@Composable
fun QiblaCompassCard(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    val state by viewModel.prayerTimesState.collectAsStateWithLifecycle()

    // Determine current location coordinates for Qibla calculation
    val (latitude, longitude, locationName) = remember(state) {
        when (val st = state) {
            is PrayerTimesState.Success -> {
                Triple(st.latitude, st.longitude, st.city)
            }
            else -> {
                // Fallback to London coordinates if location not resolved yet
                Triple(51.5074, -0.1278, "Fallback (London)")
            }
        }
    }

    // Calculate Qibla bearing
    val qiblaDirection = remember(latitude, longitude) {
        // Makkah coordinates: Lat 21.4225, Lon 39.8262
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(21.4225)
        val kaabaLonRad = Math.toRadians(39.8262)

        val deltaLon = kaabaLonRad - lonRad
        val y = Math.sin(deltaLon)
        val x = Math.cos(latRad) * Math.tan(kaabaLatRad) - Math.sin(latRad) * Math.cos(deltaLon)
        val qiblaRad = Math.atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad).toFloat()
        if (qiblaDeg < 0) {
            qiblaDeg += 360f
        }
        qiblaDeg
    }

    val distanceToKaaba = remember(latitude, longitude) {
        val earthRadiusKm = 6371.0
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(21.4225)
        val kaabaLonRad = Math.toRadians(39.8262)

        val dLat = kaabaLatRad - latRad
        val dLon = kaabaLonRad - lonRad

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(latRad) * Math.cos(kaabaLatRad) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        earthRadiusKm * c
    }

    // Sensor State
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    var rawAzimuth by remember { mutableStateOf(0f) }
    var sensorAccuracy by remember { mutableStateOf(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    // State to keep track of shortest path orientation changes and avoid 360/0 jump spins
    var continuousAzimuth by remember { mutableStateOf(0f) }
    var lastAzimuth by remember { mutableStateOf(0f) }

    LaunchedEffect(rawAzimuth) {
        var diff = rawAzimuth - lastAzimuth
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        continuousAzimuth += diff
        lastAzimuth = rawAzimuth
    }

    // Dynamic butter-smooth rotation animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = continuousAzimuth,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        )
    )

    DisposableEffect(sensorManager) {
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        
        val rotationListener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    val azimuthRad = orientationValues[0]
                    var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    if (azimuthDeg < 0) {
                        azimuthDeg += 360f
                    }
                    rawAzimuth = azimuthDeg
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        val backupListener = object : SensorEventListener {
            val lastAccelerometer = FloatArray(3)
            val lastMagnetometer = FloatArray(3)
            var lastAccelerometerSet = false
            var lastMagnetometerSet = false

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }

                if (lastAccelerometerSet && lastMagnetometerSet) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, lastAccelerometer, lastMagnetometer)) {
                        val orientationValues = FloatArray(3)
                        SensorManager.getOrientation(r, orientationValues)
                        val azimuthRad = orientationValues[0]
                        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        if (azimuthDeg < 0) {
                            azimuthDeg += 360f
                        }
                        rawAzimuth = azimuthDeg
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(rotationListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accel != null && mag != null) {
                sensorManager.registerListener(backupListener, accel, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(backupListener, mag, SensorManager.SENSOR_DELAY_UI)
            } else {
                isSensorAvailable = false
            }
        }

        onDispose {
            sensorManager.unregisterListener(rotationListener)
            sensorManager.unregisterListener(backupListener)
        }
    }

    // Determine if phone is pointed nicely towards Qibla (within ±6 degrees tolerance)
    val angleDifference = remember(rawAzimuth, qiblaDirection) {
        var diff = qiblaDirection - rawAzimuth
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        Math.abs(diff)
    }
    val isAligned = angleDifference <= 6f

    val cardBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F172A).copy(alpha = 0.9f),
                Color(0xFF020617).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF8FAFC).copy(alpha = 0.95f),
                Color(0xFFF1F5F9).copy(alpha = 0.95f)
            )
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val boundaryColor = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFCBD5E1).copy(alpha = 0.8f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("qibla_compass_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, if (isAligned) GoldBronze.copy(alpha = 0.8f) else boundaryColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAligned) GoldBronze.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Compass Icon",
                                tint = if (isAligned) GoldBronze else primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Qibla Compass",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAligned) GoldBronze else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Makkah Compass Direction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAligned) GoldBronze.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, if (isAligned) GoldBronze else Color.Transparent)
                    ) {
                        Text(
                            text = if (isAligned) "ALIGNED" else "ROTATE PHONE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) GoldBronze else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isSensorAvailable) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Sensor warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Magnetic sensor not found on this device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().testTag("qibla_compass_canvas")) {
                            val center = this.size.center
                            val radius = this.size.minDimension / 2f
                            
                            drawCircle(
                                color = if (isAligned) GoldBronze.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.05f),
                                radius = radius,
                                style = Stroke(width = 8.dp.toPx())
                            )

                            drawCircle(
                                color = if (isAligned) GoldBronze.copy(alpha = 0.4f) else boundaryColor.copy(alpha = 0.5f),
                                radius = radius - 4.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            rotate(-animatedAzimuth, pivot = center) {
                                for (angle in 0 until 360 step 30) {
                                    val tickLength = if (angle % 90 == 0) 10.dp.toPx() else 6.dp.toPx()
                                    val startX = center.x + (radius - 12.dp.toPx()) * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
                                    val startY = center.y - (radius - 12.dp.toPx()) * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
                                    val endX = center.x + (radius - 12.dp.toPx() - tickLength) * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
                                    val endY = center.y - (radius - 12.dp.toPx() - tickLength) * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
                                    
                                    drawLine(
                                        color = if (angle % 90 == 0) GoldBronze.copy(alpha = 0.6f) else boundaryColor.copy(alpha = 0.5f),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = if (angle % 90 == 0) 2.dp.toPx() else 1.dp.toPx()
                                    )
                                }

                                val paint = android.graphics.Paint().apply {
                                    color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    textSize = 12.sp.toPx()
                                    isAntiAlias = true
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                }
                                val redPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#EF4444")
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    textSize = 13.sp.toPx()
                                    isAntiAlias = true
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                }

                                drawContext.canvas.nativeCanvas.drawText("N", center.x, center.y - radius + 24.dp.toPx(), redPaint)
                                drawContext.canvas.nativeCanvas.drawText("S", center.x, center.y + radius - 14.dp.toPx(), paint)
                                drawContext.canvas.nativeCanvas.drawText("E", center.x + radius - 14.dp.toPx(), center.y + 4.dp.toPx(), paint)
                                drawContext.canvas.nativeCanvas.drawText("W", center.x - radius + 14.dp.toPx(), center.y + 4.dp.toPx(), paint)

                                rotate(qiblaDirection, pivot = center) {
                                    val needlePath = Path().apply {
                                        moveTo(center.x, center.y - radius + 22.dp.toPx())
                                        lineTo(center.x - 12.dp.toPx(), center.y - radius + 55.dp.toPx())
                                        lineTo(center.x - 4.dp.toPx(), center.y - radius + 48.dp.toPx())
                                        lineTo(center.x - 4.dp.toPx(), center.y - 15.dp.toPx())
                                        lineTo(center.x + 4.dp.toPx(), center.y - 15.dp.toPx())
                                        lineTo(center.x + 4.dp.toPx(), center.y - radius + 48.dp.toPx())
                                        lineTo(center.x + 12.dp.toPx(), center.y - radius + 55.dp.toPx())
                                        close()
                                    }
                                    
                                    drawPath(
                                        path = needlePath,
                                        color = if (isAligned) GoldBronze else primaryColor
                                    )

                                    val kaabaWidth = 14.dp.toPx()
                                    val kaabaY = center.y - radius + 56.dp.toPx()
                                    
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(center.x - kaabaWidth / 2, kaabaY),
                                        size = androidx.compose.ui.geometry.Size(kaabaWidth, kaabaWidth)
                                    )
                                    drawRect(
                                        color = GoldBronze,
                                        topLeft = Offset(center.x - kaabaWidth / 2, kaabaY + kaabaWidth * 0.25f),
                                        size = androidx.compose.ui.geometry.Size(kaabaWidth, kaabaWidth * 0.15f)
                                    )
                                }
                            }

                            drawLine(
                                color = if (isAligned) GoldBronze else primaryColor.copy(alpha = 0.6f),
                                start = Offset(center.x, center.y - radius - 2.dp.toPx()),
                                end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            drawCircle(
                                color = if (isDark) Color(0xFF1E293B) else Color.White,
                                radius = 18.dp.toPx()
                            )
                            drawCircle(
                                color = if (isAligned) GoldBronze else primaryColor,
                                radius = 12.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = if (isAligned) GoldBronze else primaryColor.copy(alpha = 0.4f),
                                radius = 6.dp.toPx()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Qibla Bearing",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                String.format("%.2f°", qiblaDirection),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAligned) GoldBronze else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(boundaryColor)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "My Heading",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                String.format("%.0f°", rawAzimuth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(boundaryColor)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Distance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                if (distanceToKaaba > 15000.0) "Mecca" else String.format("%,.0f km", distanceToKaaba),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Location: $locationName",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuranDevFooter(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val goldOrSecondary = if (isDark) GoldBronze else MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp)
            .testTag("quran_dev_footer"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .width(100.dp)
                .padding(bottom = 16.dp),
            thickness = 2.dp,
            color = GoldBronze.copy(alpha = 0.3f)
        )
        
        Text(
            text = "(تم التصميم و البرمجة بواسطة العبد الفقير الى الله رامي الجنزوري - صدقة جارية عن امي و ابي و كل عباد الله )",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 8.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(goldOrSecondary.copy(alpha = 0.1f))
                .clickable {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:+201224278430")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "+201224278430", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Contact phone",
                tint = goldOrSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+201224278430",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = goldOrSecondary
            )
        }
    }
}

@Composable
fun QuranWelcomeDialog(
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val goldColor = GoldBronze

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_dismiss_button")
            ) {
                Text(
                    text = "بسم الله الرحمن الرحيم",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(goldColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Welcome Logo",
                        tint = goldColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "إهداء وتقدير",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "(تم التصميم و البرمجة بواسطة العبد الفقير الى الله رامي الجنزوري - صدقة جارية عن امي و ابي و كل عباد الله )",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(goldColor.copy(alpha = 0.1f))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:+201224278430")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "+201224278430", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Contact phone",
                        tint = goldColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+201224278430",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp
    )
}

@Composable
fun ThemeSelectorRow(
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val themeModesList = listOf(
            AppThemeMode.LIGHT to Pair(Color(0xFFFFFFFF), "Light"), 
            AppThemeMode.DARK to Pair(Color(0xFF0A0C0A), "Dark"), 
            AppThemeMode.SEPIA to Pair(Color(0xFFFBF0D9), "Sepia")
        )

        themeModesList.forEach { (mode, colorAndName) ->
            val (previewColor, labelName) = colorAndName
            val isSelected = currentTheme == mode
            
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            val border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }

            Card(
                onClick = { onThemeSelected(mode) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("theme_button_${mode.name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg, contentColor = fg),
                border = border
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Colour preview circle
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(previewColor)
                                .border(1.dp, if (mode == AppThemeMode.LIGHT) Color.Gray.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = labelName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranslationSelectorRow(
    currentLang: com.example.ui.viewmodel.TranslationLanguage,
    onLangSelected: (com.example.ui.viewmodel.TranslationLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val langsList = listOf(
            com.example.ui.viewmodel.TranslationLanguage.ENGLISH,
            com.example.ui.viewmodel.TranslationLanguage.URDU
        )

        langsList.forEach { lang ->
            val isSelected = currentLang == lang
            
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            val border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }

            Card(
                onClick = { onLangSelected(lang) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("lang_button_${lang.name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg, contentColor = fg),
                border = border
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lang.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatMsToTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun SurahAudioPlayerBar(
    viewModel: QuranViewModel,
    surah: Surah,
    modifier: Modifier = Modifier
) {
    val verses by viewModel.versesList.collectAsStateWithLifecycle()
    val playingVerseId by viewModel.playingVerseId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val downloadedVerses by viewModel.downloadedVerses.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()

    val activeVerse = remember(playingVerseId, verses) {
        verses.find { it.id == playingVerseId }
    }

    val playerBgCard = when (currentTheme) {
        AppThemeMode.DARK -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        AppThemeMode.LIGHT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        AppThemeMode.SEPIA -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    }

    val contentColor = when (currentTheme) {
        AppThemeMode.DARK -> MaterialTheme.colorScheme.onPrimaryContainer
        AppThemeMode.LIGHT -> MaterialTheme.colorScheme.onPrimaryContainer
        AppThemeMode.SEPIA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("surah_audio_player_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = playerBgCard, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (activeVerse != null) "Reciting Verse ${activeVerse.verseNumber} • ${surah.englishName}" else "Whole Surah Recitation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (activeVerse != null) {
                            val key = "${surah.number}_${activeVerse.verseNumber}"
                            if (downloadedVerses.contains(key)) "Playing from Local Cache (Offline)" else "Streaming Recitation (Online)"
                        } else {
                            "Sheikh Mishary Alafasy"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isDownloading) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = downloadProgress,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                } else {
                    val allCached = remember(downloadedVerses, verses) {
                        verses.isNotEmpty() && verses.all { verse ->
                            downloadedVerses.contains("${surah.number}_${verse.verseNumber}")
                        }
                    }

                    if (allCached) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Cached",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.downloadSurahAudio(surah.number) },
                            modifier = Modifier.testTag("download_surah_audio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Download Surah Audio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val sliderPosition = if (duration > 0) currentPosition.toFloat() else 0f
            val maxSliderValue = if (duration > 0) duration.toFloat() else 100f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMsToTime(currentPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )

                Slider(
                    value = sliderPosition,
                    onValueChange = { viewModel.seekTo(it.toInt()) },
                    valueRange = 0f..maxSliderValue,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("audio_playback_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = contentColor.copy(alpha = 0.24f)
                    )
                )

                Text(
                    text = formatMsToTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.playPreviousVerse() },
                    enabled = activeVerse != null,
                    modifier = Modifier.testTag("audio_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Verse",
                        modifier = Modifier.size(24.dp),
                        tint = if (activeVerse != null) contentColor else contentColor.copy(alpha = 0.38f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FilledIconButton(
                    onClick = { viewModel.togglePlayPauseSurah() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("audio_play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { viewModel.playNextVerse() },
                    enabled = activeVerse != null,
                    modifier = Modifier.testTag("audio_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Verse",
                        modifier = Modifier.size(24.dp),
                        tint = if (activeVerse != null) contentColor else contentColor.copy(alpha = 0.38f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { viewModel.stopAudio() },
                    enabled = activeVerse != null,
                    modifier = Modifier.testTag("audio_stop_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Stop",
                        modifier = Modifier.size(20.dp),
                        tint = if (activeVerse != null) contentColor else contentColor.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

