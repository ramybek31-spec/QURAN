package com.example.data.repository

import com.example.data.database.BookmarkEntity
import com.example.data.database.QuranDao
import com.example.data.database.SyncStateEntity
import com.example.data.database.VerseEntity
import com.example.data.database.VerseTafsirEntity
import com.example.data.model.PreloadedQuranData
import com.example.data.network.QuranApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class QuranRepository(
    private val quranDao: QuranDao,
    private val apiService: QuranApiService,
    private val prayerApiService: com.example.data.network.PrayerTimesApiService = com.example.data.network.PrayerTimesApiService.create()
) {
    val allBookmarks: Flow<List<BookmarkEntity>> = quranDao.getAllBookmarks()
    val syncState: Flow<SyncStateEntity?> = quranDao.getSyncState()
    val totalCachedCount: Flow<Int> = quranDao.getCachedVersesCount()

    suspend fun fetchIpLocation(): Result<com.example.data.network.IpLocationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = prayerApiService.getIpLocation("https://ipapi.co/json/")
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchPrayerTimes(latitude: Double, longitude: Double): Result<com.example.data.network.AladhanPrayerResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.aladhan.com/v1/timings?latitude=$latitude&longitude=$longitude&method=2"
                val response = prayerApiService.getPrayerTimesByCoordinates(url)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Retrieve verses. If empty in DB, try to pre-populate from offline core dataset!
    fun getVerses(surahNumber: Int): Flow<List<VerseEntity>> {
        return quranDao.getVersesForSurah(surahNumber)
    }

    fun getTafsirForVerse(surahNumber: Int, verseNumber: Int): Flow<VerseTafsirEntity?> {
        return quranDao.getTafsirForVerse("${surahNumber}_${verseNumber}")
    }

    suspend fun checkAndPrepopulate(surahNumber: Int) {
        withContext(Dispatchers.IO) {
            val existing = quranDao.getVersesForSurah(surahNumber).first()
            if (existing.isEmpty()) {
                val preloaded = PreloadedQuranData.verses.filter { it.surahNumber == surahNumber }
                if (preloaded.isNotEmpty()) {
                    val entities = preloaded.map {
                        val paddedS = it.surahNumber.toString().padStart(3, '0')
                        val paddedV = it.verseNumber.toString().padStart(3, '0')
                        VerseEntity(
                            id = "${it.surahNumber}_${it.verseNumber}",
                            surahNumber = it.surahNumber,
                            verseNumber = it.verseNumber,
                            textArabic = it.textArabic,
                            textEnglish = it.textEnglish,
                            textUrdu = it.textUrdu,
                            audioUrl = "https://everyayah.com/data/Alafasy/$paddedS$paddedV.mp3"
                        )
                    }
                    quranDao.insertVerses(entities)

                    val preloadedTafsirs = PreloadedQuranData.tafsirs.filter { it.surahNumber == surahNumber }
                    if (preloadedTafsirs.isNotEmpty()) {
                        val tafsirEntities = preloadedTafsirs.map {
                            VerseTafsirEntity(
                                id = "${it.surahNumber}_${it.verseNumber}",
                                surahNumber = it.surahNumber,
                                verseNumber = it.verseNumber,
                                tafsirText = it.tafsirText
                            )
                        }
                        quranDao.insertTafsirs(tafsirEntities)
                    }
                }
            }
        }
    }

    // Toggle bookmark status
    suspend fun toggleBookmark(surahNumber: Int, verseNumber: Int) {
        withContext(Dispatchers.IO) {
            val id = "${surahNumber}_${verseNumber}"
            val bookmarks = quranDao.getAllBookmarks().first()
            val exists = bookmarks.any { it.id == id }
            if (exists) {
                quranDao.deleteBookmarkById(id)
            } else {
                quranDao.insertBookmark(BookmarkEntity(id = id, surahNumber = surahNumber, verseNumber = verseNumber))
            }
        }
    }

    fun isBookmarkedFlow(surahNumber: Int, verseNumber: Int): Flow<Boolean> {
        val id = "${surahNumber}_${verseNumber}"
        return quranDao.isBookmarked(id)
    }

    // Fetch from AlQuran Cloud and Cache into SQLite
    suspend fun fetchAndCacheSurah(surahNumber: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSurahContent(surahNumber)
                if (response.code == 200 && response.data.size >= 2) {
                    val arabicData = response.data[0]
                    val englishData = response.data[1]
                    val urduData = if (response.data.size >= 3) response.data[2] else null

                    val versesToCache = arabicData.ayahs.mapIndexed { index, arabicAyah ->
                        val englishAyah = englishData.ayahs[index]
                        val urduAyahText = if (urduData != null && index < urduData.ayahs.size) urduData.ayahs[index].text else ""
                        val paddedS = surahNumber.toString().padStart(3, '0')
                        val paddedV = arabicAyah.numberInSurah.toString().padStart(3, '0')

                        VerseEntity(
                            id = "${surahNumber}_${arabicAyah.numberInSurah}",
                            surahNumber = surahNumber,
                            verseNumber = arabicAyah.numberInSurah,
                            textArabic = arabicAyah.text,
                            textEnglish = englishAyah.text,
                            textUrdu = urduAyahText,
                            audioUrl = "https://everyayah.com/data/Alafasy/$paddedS$paddedV.mp3"
                        )
                    }

                    quranDao.insertVerses(versesToCache)

                    val tafsirsToCache = if (response.data.size >= 4) {
                        val tafsirEdition = response.data[3]
                        tafsirEdition.ayahs.map { ayah ->
                            VerseTafsirEntity(
                                id = "${surahNumber}_${ayah.numberInSurah}",
                                surahNumber = surahNumber,
                                verseNumber = ayah.numberInSurah,
                                tafsirText = ayah.text
                            )
                        }
                    } else {
                        arabicData.ayahs.map { ayah ->
                            VerseTafsirEntity(
                                id = "${surahNumber}_${ayah.numberInSurah}",
                                surahNumber = surahNumber,
                                verseNumber = ayah.numberInSurah,
                                tafsirText = "Study exegesis for Verse $surahNumber:${ayah.numberInSurah}. Dive deep into the historical context, word-by-word linguistics, and the divine message of this Chapter."
                            )
                        }
                    }
                    quranDao.insertTafsirs(tafsirsToCache)

                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Invalid response format from AlQuran API"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun searchVerses(query: String): Flow<List<VerseEntity>> {
        return quranDao.searchVersesOffline(query)
    }

    // Simulated cloud backup & cross-device restore synchronization module
    suspend fun syncWithCloud(email: String, deviceName: String): Result<SyncStateEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // In a perfect mobile application layout, we display real sync updates
                // and store bookmark counts. Let's simulate a cloud request with a small safe delay.
                kotlinx.coroutines.delay(1500)

                val bookmarks = quranDao.getAllBookmarks().first()
                val currentSyncState = quranDao.getSyncState().first()
                val newSyncCount = (currentSyncState?.syncCount ?: 0) + 1

                val newState = SyncStateEntity(
                    lastSyncedTimestamp = System.currentTimeMillis(),
                    deviceName = deviceName,
                    syncCount = newSyncCount
                )

                quranDao.insertSyncState(newState)
                Result.success(newState)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
