package br.com.sd.pixelhubandroid.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL =
    "http://10.0.2.2:8080"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object PixelHubApi {
    val retrofitService: PixelHubApiMethods by lazy {
        retrofit.create(PixelHubApiMethods::class.java)
    }
}
