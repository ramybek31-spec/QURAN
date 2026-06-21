package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_verses")
data class VerseEntity(
    @PrimaryKey val id: String, // format "surah_verse" (e.g., "1_1")
    val surahNumber: Int,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val textUrdu: String = "",
    val audioUrl: String? = null
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // format "surah_verse" (e.g., "1_1")
    val surahNumber: Int,
    val verseNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_history")
data class SyncStateEntity(
    @PrimaryKey val id: String = "primary_sync",
    val lastSyncedTimestamp: Long,
    val deviceName: String,
    val syncCount: Int
)

@Entity(tableName = "verse_tafsir")
data class VerseTafsirEntity(
    @PrimaryKey val id: String, // format "surah_verse" (e.g., "1_1")
    val surahNumber: Int,
    val verseNumber: Int,
    val tafsirText: String
)

