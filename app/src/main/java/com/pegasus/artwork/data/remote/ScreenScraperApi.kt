package com.pegasus.artwork.data.remote

import com.pegasus.artwork.data.remote.dto.ScreenScraperResponse
import com.pegasus.artwork.data.remote.dto.UserInfoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScreenScraperApi {

    @GET("api2/jeuInfos.php")
    suspend fun getGameInfo(
        @Query("devid") devId: String,
        @Query("devpassword") devPassword: String,
        @Query("softname") softName: String,
        @Query("output") output: String = "json",
        @Query("ssid") ssId: String? = null,
        @Query("sspassword") ssPassword: String? = null,
        @Query("systemeid") systemId: Int,
        @Query("romnom") romName: String? = null,
        @Query("crc") crc: String? = null,
        @Query("md5") md5: String? = null,
        @Query("sha1") sha1: String? = null,
        @Query("romtaille") romSize: Long? = null,
    ): Response<ScreenScraperResponse>

    @GET("api2/ssuserInfos.php")
    suspend fun getUserInfo(
        @Query("devid") devId: String,
        @Query("devpassword") devPassword: String,
        @Query("softname") softName: String,
        @Query("output") output: String = "json",
        @Query("ssid") ssId: String,
        @Query("sspassword") ssPassword: String,
    ): Response<UserInfoResponse>
}
