package br.com.sd.pixelhubandroid.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PixelHubApi {
    private var retrofit: Retrofit? = null
    private var currentIp: String? = null

    fun getRetrofitService(serverIp: String): PixelHubApiMethods {
        if (retrofit == null || currentIp != serverIp) {
            currentIp = serverIp
            retrofit = Retrofit.Builder()
                .baseUrl("http://$serverIp:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(PixelHubApiMethods::class.java)
    }
}
