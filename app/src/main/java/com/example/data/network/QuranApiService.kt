package com.example.data.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AyahData(
    val number: Int,
    val text: String,
    val numberInSurah: Int
)

@JsonClass(generateAdapter = true)
data class EditionData(
    val number: Int,
    val name: String,
    val englishName: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahData>
)

@JsonClass(generateAdapter = true)
data class QuranEditionResponse(
    val code: Int,
    val status: String,
    val data: List<EditionData>
)

interface QuranApiService {
    @GET("surah/{surahNumber}/editions/quran-uthmani,en.sahih,ur.maududi,en.tafsir.rezaandrei")
    suspend fun getSurahContent(
        @Path("surahNumber") surahNumber: Int
    ): QuranEditionResponse

    companion object {
        private const val BASE_URL = "https://api.alquran.cloud/v1/"

        fun create(): QuranApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
