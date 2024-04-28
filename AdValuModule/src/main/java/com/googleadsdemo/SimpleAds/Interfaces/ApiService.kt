package com.googleadsdemo.SimpleAds.Interfaces


import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api.php")
    fun fetchData(@Body requestBody: RequestBody): Call<String>
}

