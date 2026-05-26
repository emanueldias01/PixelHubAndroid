package br.com.sd.pixelhubandroid.network

import br.com.sd.pixelhubandroid.data.data.AuthData
import br.com.sd.pixelhubandroid.data.data.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PixelHubApiMethods {

    @POST("login")
    suspend fun login(@Body loginRequest: AuthData): LoginResponse

    @POST("logout")
    suspend fun logout(@Body logoutRequest: AuthData): LoginResponse

}
