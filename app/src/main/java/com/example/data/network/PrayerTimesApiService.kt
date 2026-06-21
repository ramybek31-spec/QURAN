package com.example.data.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class IpLocationResponse(
    val ip: String? = null,
    val city: String? = null,
    val region: String? = null,
    val country_name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanPrayerResponse(
    val code: Int,
    val status: String,
    val data: AladhanData
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    val timings: PrayerTimings,
    val date: AladhanDate,
    val meta: AladhanMeta
)

@JsonClass(generateAdapter = true)
data class PrayerTimings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String,
    val Midnight: String
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val readable: String,
    val hijri: AladhanHijri? = null
)

@JsonClass(generateAdapter = true)
data class AladhanHijri(
    val date: String,
    val format: String,
    val day: String,
    val weekday: AladhanWeekday? = null,
    val month: AladhanMonth? = null,
    val year: String
)

@JsonClass(generateAdapter = true)
data class AladhanWeekday(
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanMonth(
    val number: Int,
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanMeta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)

interface PrayerTimesApiService {
    @GET
    suspend fun getIpLocation(@Url url: String): IpLocationResponse

    @GET
    suspend fun getPrayerTimesByCoordinates(@Url url: String): AladhanPrayerResponse

    companion object {
        fun create(): PrayerTimesApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api.aladhan.com/v1/") // Default placeholder, overwritten by @Url
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(PrayerTimesApiService::class.java)
        }
    }
}
