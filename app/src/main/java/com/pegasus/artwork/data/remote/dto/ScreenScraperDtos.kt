package com.pegasus.artwork.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScreenScraperResponse(
    @SerialName("response") val response: ResponseBody,
)

@Serializable
data class ResponseBody(
    @SerialName("ssuser") val ssuser: SsUserDto? = null,
    @SerialName("jeu") val jeu: GameInfoDto,
)

@Serializable
data class UserInfoResponse(
    @SerialName("response") val response: UserInfoBody,
)

@Serializable
data class UserInfoBody(
    @SerialName("ssuser") val ssuser: SsUserDto,
)

@Serializable
data class SsUserDto(
    @SerialName("id") val id: String = "",
    @SerialName("maxthreads") val maxthreads: Int = 1,
)

@Serializable
data class GameInfoDto(
    @SerialName("id") val id: String = "",
    @SerialName("noms") val names: List<GameNameDto> = emptyList(),
    @SerialName("medias") val medias: List<MediaDto> = emptyList(),
)

@Serializable
data class GameNameDto(
    @SerialName("region") val region: String = "",
    @SerialName("text") val text: String = "",
)

@Serializable
data class MediaDto(
    @SerialName("type") val type: String = "",
    @SerialName("region") val region: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("format") val format: String = "",
)
