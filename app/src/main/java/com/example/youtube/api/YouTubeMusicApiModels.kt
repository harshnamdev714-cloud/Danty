package com.example.youtube.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InvidiousSearchResultDto(
    @Json(name = "type") val type: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "authorId") val authorId: String? = null,
    @Json(name = "authorUrl") val authorUrl: String? = null,
    @Json(name = "lengthSeconds") val lengthSeconds: Int? = null,
    @Json(name = "videoThumbnails") val videoThumbnails: List<InvidiousThumbnailDto>? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "viewCount") val viewCount: Long? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousThumbnailDto(
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousVideoDetailDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "lengthSeconds") val lengthSeconds: Int? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "formatStreams") val formatStreams: List<InvidiousStreamFormatDto>? = null,
    @Json(name = "adaptiveFormats") val adaptiveFormats: List<InvidiousStreamFormatDto>? = null
)

@JsonClass(generateAdapter = true)
data class InvidiousStreamFormatDto(
    @Json(name = "url") val url: String? = null,
    @Json(name = "itag") val itag: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "bitrate") val bitrate: String? = null,
    @Json(name = "container") val container: String? = null,
    @Json(name = "encoding") val encoding: String? = null,
    @Json(name = "qualityLabel") val qualityLabel: String? = null,
    @Json(name = "audioSampleRate") val audioSampleRate: Int? = null,
    @Json(name = "audioChannels") val audioChannels: Int? = null
)
