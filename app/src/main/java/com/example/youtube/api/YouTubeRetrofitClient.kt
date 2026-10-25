package com.example.youtube.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object YouTubeRetrofitClient {

    val INSTANCES = listOf(
        "https://inv.tux.pizza/",
        "https://invidious.nerdvpn.de/",
        "https://vid.puffyan.us/",
        "https://yewtu.be/"
    )

    var currentBaseUrl: String = INSTANCES.first()
        private set

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; ResonanceHiFi/1.0)")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var cachedService: YouTubeMusicApiService? = null

    fun getService(baseUrl: String = currentBaseUrl): YouTubeMusicApiService {
        if (cachedService != null && currentBaseUrl == baseUrl) {
            return cachedService!!
        }

        currentBaseUrl = baseUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(YouTubeMusicApiService::class.java)
        cachedService = service
        return service
    }

    fun switchInstance(newInstanceUrl: String) {
        currentBaseUrl = if (newInstanceUrl.endsWith("/")) newInstanceUrl else "$newInstanceUrl/"
        cachedService = null
    }
}
