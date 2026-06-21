package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>

    // --- Verses ---
    @Query("SELECT * FROM cached_verses WHERE surahNumber = :surahNumber ORDER BY verseNumber ASC")
    fun getVersesForSurah(surahNumber: Int): Flow<List<VerseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Query("SELECT * FROM cached_verses WHERE id = :id")
    suspend fun getVerseById(id: String): VerseEntity?

    @Query("SELECT * FROM cached_verses WHERE textEnglish LIKE '%' || :query || '%' OR textArabic LIKE '%' || :query || '%'")
    fun searchVersesOffline(query: String): Flow<List<VerseEntity>>

    @Query("SELECT COUNT(*) FROM cached_verses")
    fun getCachedVersesCount(): Flow<Int>

    // --- Tafsir ---
    @Query("SELECT * FROM verse_tafsir WHERE id = :id")
    fun getTafsirForVerse(id: String): Flow<VerseTafsirEntity?>

    @Query("SELECT * FROM verse_tafsir WHERE id = :id")
    suspend fun getTafsirForVerseOneShot(id: String): VerseTafsirEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsirs(tafsirs: List<VerseTafsirEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(tafsir: VerseTafsirEntity)

    // --- Sync State ---
    @Query("SELECT * FROM sync_history WHERE id = :id")
    fun getSyncState(id: String = "primary_sync"): Flow<SyncStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncState(state: SyncStateEntity)
}
