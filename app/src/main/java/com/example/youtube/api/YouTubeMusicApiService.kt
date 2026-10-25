package com.example.youtube.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YouTubeMusicApiService {

    @GET("api/v1/search")
    suspend fun searchMusic(
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("sort_by") sortBy: String = "relevance"
    ): List<InvidiousSearchResultDto>

    @GET("api/v1/videos/{videoId}")
    suspend fun getVideoDetails(
        @Path("videoId") videoId: String
    ): InvidiousVideoDetailDto
}
