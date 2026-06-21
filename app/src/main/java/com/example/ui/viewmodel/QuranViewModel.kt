package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.BookmarkEntity
import com.example.data.database.QuranDatabase
import com.example.data.database.SyncStateEntity
import com.example.data.database.VerseEntity
import com.example.data.model.Surah
import com.example.data.model.SurahMetadata
import com.example.data.network.QuranApiService
import com.example.data.repository.QuranRepository
import com.example.ui.theme.AppThemeMode
import java.io.File
import java.net.URL
import java.net.HttpURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TranslationLanguage(val displayName: String) {
    ENGLISH("English"),
    URDU("Urdu (اردو)")
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuranDatabase.getDatabase(application)
    private val repository = QuranRepository(
        quranDao = database.quranDao(),
        apiService = QuranApiService.create()
    )

    // --- State variables ---
    val allSurahs: List<Surah> = SurahMetadata.list

    // Theme Mode settings
    private val _themeMode = MutableStateFlow<AppThemeMode>(
        try {
            val savedMode = application.getSharedPreferences("quran_reading_progress", Context.MODE_PRIVATE)
                .getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
            AppThemeMode.valueOf(savedMode)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun updateThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        getApplication<Application>().getSharedPreferences("quran_reading_progress", Context.MODE_PRIVATE)
            .edit().putString("theme_mode", mode.name).apply()
    }

    // Translation Language Settings
    private val _translationLanguage = MutableStateFlow<TranslationLanguage>(
        try {
            val savedLang = application.getSharedPreferences("quran_reading_progress", Context.MODE_PRIVATE)
                .getString("translation_lang", TranslationLanguage.ENGLISH.name) ?: TranslationLanguage.ENGLISH.name
            TranslationLanguage.valueOf(savedLang)
        } catch (e: Exception) {
            TranslationLanguage.ENGLISH
        }
    )
    val translationLanguage: StateFlow<TranslationLanguage> = _translationLanguage.asStateFlow()

    fun updateTranslationLanguage(lang: TranslationLanguage) {
        _translationLanguage.value = lang
        getApplication<Application>().getSharedPreferences("quran_reading_progress", Context.MODE_PRIVATE)
            .edit().putString("translation_lang", lang.name).apply()
    }

    // Audio download / offline cached states
    private val audioDir = File(application.filesDir, "audio_cache").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    private val _downloadedVerses = MutableStateFlow<Set<String>>(emptySet())
    val downloadedVerses: StateFlow<Set<String>> = _downloadedVerses.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    init {
        refreshDownloadedVerses()
    }

    fun getAudioFile(surahNumber: Int, verseNumber: Int): File {
        return File(audioDir, "recitation_${surahNumber}_$verseNumber.mp3")
    }

    fun refreshDownloadedVerses() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = audioDir.listFiles() ?: emptyArray()
            val downloadedSet = files.filter { it.name.startsWith("recitation_") && it.name.endsWith(".mp3") }
                .map { file ->
                    file.nameWithoutExtension.substringAfter("recitation_")
                }.toSet()
            _downloadedVerses.value = downloadedSet
        }
    }

    // Reading Progress States
    private val sharedPrefs = application.getSharedPreferences("quran_reading_progress", Context.MODE_PRIVATE)
    private val _surahProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val surahProgress: StateFlow<Map<Int, Int>> = _surahProgress.asStateFlow()

    private fun loadReadingProgress() {
        val progressMap = mutableMapOf<Int, Int>()
        for (i in 1..114) {
            val percentage = sharedPrefs.getInt("surah_$i", 0)
            if (percentage > 0) {
                progressMap[i] = percentage
            }
        }
        _surahProgress.value = progressMap
    }

    fun saveSurahProgress(surahNumber: Int, percentage: Int) {
        val coerced = percentage.coerceIn(0, 100)
        val currentSaved = _surahProgress.value[surahNumber] ?: 0
        if (coerced > currentSaved) {
            sharedPrefs.edit().putInt("surah_$surahNumber", coerced).apply()
            _surahProgress.value = _surahProgress.value.toMutableMap().apply {
                put(surahNumber, coerced)
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered surahs list based on search bar
    val filteredSurahs: StateFlow<List<Surah>> = _searchQuery
        .map { query ->
            if (query.isEmpty()) {
                allSurahs
            } else {
                allSurahs.filter {
                    it.englishName.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.englishNameTranslation.contains(query, ignoreCase = true) ||
                    it.number.toString() == query
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allSurahs)

    // Active Reader State
    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _versesList = MutableStateFlow<List<VerseEntity>>(emptyList())
    val versesList: StateFlow<List<VerseEntity>> = _selectedSurah
        .flatMapLatest { surah ->
            if (surah == null) {
                flowOf(emptyList())
            } else {
                repository.getVerses(surah.number)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoadingSurah = MutableStateFlow(false)
    val isLoadingSurah: StateFlow<Boolean> = _isLoadingSurah.asStateFlow()

    private val _surahError = MutableStateFlow<String?>(null)
    val surahError: StateFlow<String?> = _surahError.asStateFlow()

    // Bookmarks Flow
    val bookmarkedVerses: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cache Stats
    val totalCachedCount: StateFlow<Int> = repository.totalCachedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Settings State
    private val _arabicFontSize = MutableStateFlow(32f) // Slider range: 24f to 48f
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    private val _englishFontSize = MutableStateFlow(16f) // Slider range: 12f to 28f
    val englishFontSize: StateFlow<Float> = _englishFontSize.asStateFlow()

    // Sync State
    val syncState: StateFlow<SyncStateEntity?> = repository.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncSuccess = MutableStateFlow<String?>(null)
    val syncSuccess: StateFlow<String?> = _syncSuccess.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    // Current general search result for matching verses
    private val _verseSearchQuery = MutableStateFlow("")
    val verseSearchQuery: StateFlow<String> = _verseSearchQuery.asStateFlow()

    val searchedVersesResult: StateFlow<List<VerseEntity>> = _verseSearchQuery
        .flatMapLatest { query ->
            if (query.trim().length < 2) {
                flowOf(emptyList())
            } else {
                repository.searchVerses(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Audio Playback Engine ---
    private var mediaPlayer: MediaPlayer? = null
    
    private val _playingVerseId = MutableStateFlow<String?>(null)
    val playingVerseId: StateFlow<String?> = _playingVerseId.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    // --- Prayer Times Engine State & Methods ---
    private val _prayerTimesState = MutableStateFlow<PrayerTimesState>(PrayerTimesState.Loading)
    val prayerTimesState: StateFlow<PrayerTimesState> = _prayerTimesState.asStateFlow()

    fun fetchPrayerTimesForLocation(lat: Double, lng: Double, city: String, country: String) {
        viewModelScope.launch {
            _prayerTimesState.value = PrayerTimesState.Loading
            repository.fetchPrayerTimes(lat, lng)
                .onSuccess { response ->
                    val timings = response.data.timings
                    val readableDate = response.data.date.readable
                    val hijri = response.data.date.hijri
                    val hijriString = if (hijri != null) {
                        "${hijri.day} ${hijri.month?.en ?: ""} ${hijri.year} AH"
                    } else {
                        ""
                    }
                    _prayerTimesState.value = PrayerTimesState.Success(
                        timings = timings,
                        city = city,
                        country = country,
                        dateReadable = readableDate,
                        hijriDate = hijriString,
                        latitude = lat,
                        longitude = lng
                    )
                }
                .onFailure { error ->
                    _prayerTimesState.value = PrayerTimesState.Error(error.localizedMessage ?: "Failed to fetch timings")
                }
        }
    }

    fun loadPrayerTimesWithFallback(lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            _prayerTimesState.value = PrayerTimesState.Loading
            if (lat != null && lng != null) {
                val locationResult = repository.fetchIpLocation()
                val city = locationResult.getOrNull()?.city ?: "Current Location"
                val country = locationResult.getOrNull()?.country_name ?: ""
                fetchPrayerTimesForLocation(lat, lng, city, country)
            } else {
                repository.fetchIpLocation()
                    .onSuccess { ipLocResponse ->
                        val targetLat = ipLocResponse.latitude ?: 51.5074
                        val targetLng = ipLocResponse.longitude ?: -0.1278
                        val city = ipLocResponse.city ?: "London"
                        val country = ipLocResponse.country_name ?: "United Kingdom"
                        fetchPrayerTimesForLocation(targetLat, targetLng, city, country)
                    }
                    .onFailure {
                        fetchPrayerTimesForLocation(51.5074, -0.1278, "London", "United Kingdom")
                    }
            }
        }
    }

    init {
        loadReadingProgress()
        // Pre-populate core offline Surahs (Al-Fatihah etc.) on initialization
        viewModelScope.launch {
            repository.checkAndPrepopulate(1)   // Al-Fatihah
            repository.checkAndPrepopulate(103) // Al-Asr
            repository.checkAndPrepopulate(108) // Al-Kawthar
            repository.checkAndPrepopulate(112) // Al-Ikhlas
            repository.checkAndPrepopulate(113) // Al-Falaq
            repository.checkAndPrepopulate(114) // An-Nas
        }
        // Auto load IP-based prayer times immediately as a quick default
        loadPrayerTimesWithFallback()
    }

    fun setSurahSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setVerseSearchQuery(query: String) {
        _verseSearchQuery.value = query
    }

    fun selectSurah(surah: Surah?) {
        // Stop any playing audio when changing Surahs
        stopAudio()
        _selectedSurah.value = surah
        _surahError.value = null

        if (surah != null) {
            viewModelScope.launch {
                _isLoadingSurah.value = true
                // Prepare preloaded static if empty in DB
                repository.checkAndPrepopulate(surah.number)
                
                // Then try to fetch fresh from AlQuran API (or offline cache fallback happens automatically)
                val result = repository.fetchAndCacheSurah(surah.number)
                if (result.isFailure) {
                    val fallbackList = repository.getVerses(surah.number).first()
                    if (fallbackList.isEmpty()) {
                        _surahError.value = "This chapter requires internet to load initially, or isn't saved yet."
                    }
                }
                _isLoadingSurah.value = false
            }
        }
    }

    fun toggleBookmark(surahNumber: Int, verseNumber: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(surahNumber, verseNumber)
        }
    }

    fun getTafsirForVerse(surahNumber: Int, verseNumber: Int): Flow<com.example.data.database.VerseTafsirEntity?> {
        return repository.getTafsirForVerse(surahNumber, verseNumber)
    }

    fun updateArabicSize(size: Float) {
        _arabicFontSize.value = size
    }

    fun updateEnglishSize(size: Float) {
        _englishFontSize.value = size
    }

    // --- Media Engine Control methods ---
    private var progressTrackerJob: Job? = null

    private fun startTrackingProgress() {
        progressTrackerJob?.cancel()
        progressTrackerJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            _currentPosition.value = mp.currentPosition
                            _duration.value = mp.duration
                        }
                    } catch (e: Exception) {
                        // ignore state errors on release
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
        _currentPosition.value = 0
        _duration.value = 0
    }

    private fun backgroundCacheVerse(verse: VerseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = getAudioFile(verse.surahNumber, verse.verseNumber)
                if (!file.exists()) {
                    val url = URL(verse.audioUrl ?: return@launch)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.inputStream.use { input ->
                        val tmpFile = File(audioDir, "tmp_${verse.surahNumber}_${verse.verseNumber}")
                        tmpFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                        tmpFile.renameTo(file)
                    }
                    refreshDownloadedVerses()
                }
            } catch (e: Exception) {
                // Ignore background caching errors to avoid breaking playback
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(positionMs)
                _currentPosition.value = positionMs
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun downloadSurahAudio(surahNumber: Int) {
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadProgress.value = 0f
            
            try {
                val verses = _versesList.value.ifEmpty {
                    repository.getVerses(surahNumber).first()
                }
                if (verses.isEmpty()) {
                    _surahError.value = "Failed to download: Verses not loaded."
                    _isDownloading.value = false
                    return@launch
                }
                
                val total = verses.size
                var downloadedCount = 0
                
                withContext(Dispatchers.IO) {
                    for (verse in verses) {
                        val file = getAudioFile(verse.surahNumber, verse.verseNumber)
                        if (!file.exists()) {
                            val urlString = verse.audioUrl ?: "https://everyayah.com/data/Alafasy/${verse.surahNumber.toString().padStart(3, '0')}${verse.verseNumber.toString().padStart(3, '0')}.mp3"
                            val url = URL(urlString)
                            val conn = url.openConnection() as HttpURLConnection
                            conn.connectTimeout = 10000
                            conn.readTimeout = 15000
                            conn.inputStream.use { input ->
                                val tmpFile = File(audioDir, "download_${verse.surahNumber}_${verse.verseNumber}.tmp")
                                tmpFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                                tmpFile.renameTo(file)
                            }
                        }
                        downloadedCount++
                        _downloadProgress.value = downloadedCount.toFloat() / total.toFloat()
                    }
                }
                _surahError.value = null
                refreshDownloadedVerses()
            } catch (e: Exception) {
                _surahError.value = "Audio download failed: ${e.localizedMessage}"
            } finally {
                _isDownloading.value = false
            }
        }
    }

    fun togglePlayPauseSurah() {
        val playingId = _playingVerseId.value
        val list = _versesList.value
        if (list.isEmpty()) return

        if (playingId != null) {
            val currentVerse = list.find { it.id == playingId }
            if (currentVerse != null) {
                playRecitation(currentVerse)
            }
        } else {
            val firstVerse = list.firstOrNull()
            if (firstVerse != null) {
                playRecitation(firstVerse)
            }
        }
    }

    fun playPreviousVerse() {
        val playingId = _playingVerseId.value ?: return
        val currentList = _versesList.value
        val currentIdx = currentList.indexOfFirst { it.id == playingId }
        val prevIdx = currentIdx - 1
        if (prevIdx in currentList.indices) {
            playRecitation(currentList[prevIdx])
        }
    }

    fun playNextVerse() {
        val playingId = _playingVerseId.value ?: return
        val currentList = _versesList.value
        val currentIdx = currentList.indexOfFirst { it.id == playingId }
        val nextIdx = currentIdx + 1
        if (nextIdx in currentList.indices) {
            playRecitation(currentList[nextIdx])
        }
    }

    fun playRecitation(verse: VerseEntity) {
        // If clicking on already playing verse, toggle pause/play
        if (_playingVerseId.value == verse.id) {
            if (_isAudioPlaying.value) {
                mediaPlayer?.pause()
                _isAudioPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isAudioPlaying.value = true
                startTrackingProgress()
            }
            return
        }

        // Otherwise, release existing player and start new
        stopAudio()

        val file = getAudioFile(verse.surahNumber, verse.verseNumber)
        val hasLocalCache = file.exists()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                if (hasLocalCache) {
                    setDataSource(file.absolutePath)
                } else {
                    val url = verse.audioUrl ?: return
                    setDataSource(url)
                    // Cache in background for offline use on subsequent plays!
                    backgroundCacheVerse(verse)
                }
                
                _playingVerseId.value = verse.id
                _isLoadingSurah.value = true
                
                setOnPreparedListener {
                    start()
                    _isAudioPlaying.value = true
                    _isLoadingSurah.value = false
                    _currentPosition.value = 0
                    _duration.value = duration
                    startTrackingProgress()
                }
                
                setOnCompletionListener {
                    _isAudioPlaying.value = false
                    _playingVerseId.value = null
                    stopTrackingProgress()
                    playNextVerseAutomatic(verse)
                }

                setOnErrorListener { _, what, extra ->
                    _isLoadingSurah.value = false
                    _isAudioPlaying.value = false
                    _playingVerseId.value = null
                    stopTrackingProgress()
                    _surahError.value = "Recitation failed. For online streams, please check your network connection ($what, $extra)"
                    true
                }

                prepareAsync()
            } catch (e: Exception) {
                _isLoadingSurah.value = false
                _isAudioPlaying.value = false
                _playingVerseId.value = null
                stopTrackingProgress()
                _surahError.value = "Failed to load audio recitation: ${e.localizedMessage}"
            }
        }
    }

    private fun playNextVerseAutomatic(currentVerse: VerseEntity) {
        val currentList = _versesList.value
        val nextIdx = currentList.indexOfFirst { it.id == currentVerse.id } + 1
        if (nextIdx in currentList.indices) {
            playRecitation(currentList[nextIdx])
        } else {
            stopAudio()
        }
    }

    fun stopAudio() {
        stopTrackingProgress()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (e: Exception) {
                // ignore
            }
            it.release()
        }
        mediaPlayer = null
        _playingVerseId.value = null
        _isAudioPlaying.value = false
    }

    // --- Cloud Sync module ---
    fun syncBackup(email: String = "ramybek31@gmail.com") {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncSuccess.value = null
            _syncError.value = null

            val deviceName = "Android Device (${android.os.Build.MODEL})"
            val result = repository.syncWithCloud(email, deviceName)

            _isSyncing.value = false
            result.onSuccess { state ->
                _syncSuccess.value = "Successfully synchronized offline reading metrics and ${bookmarkedVerses.value.size} bookmarks with Cloud server for account $email at ${java.text.DateFormat.getTimeInstance().format(state.lastSyncedTimestamp)}!"
            }
            result.onFailure { error ->
                _syncError.value = "Cloud synchronization failed: ${error.localizedMessage}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}

sealed interface PrayerTimesState {
    object Loading : PrayerTimesState
    data class Success(
        val timings: com.example.data.network.PrayerTimings,
        val city: String,
        val country: String,
        val dateReadable: String,
        val hijriDate: String,
        val latitude: Double,
        val longitude: Double
    ) : PrayerTimesState
    data class Error(val message: String) : PrayerTimesState
}
