package br.com.sd.pixelhubandroid.network

import br.com.sd.pixelhubandroid.data.data.AuthData
import br.com.sd.pixelhubandroid.data.data.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PixelHubApiMethods {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: AuthData): LoginResponse

    @POST("auth/logout")
    suspend fun logout(@Body logoutRequest: AuthData): LoginResponse

}
