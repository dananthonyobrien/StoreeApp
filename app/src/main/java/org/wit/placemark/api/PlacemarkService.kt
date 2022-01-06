package org.wit.placemark.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.wit.placemark.models.PlacemarkModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface PlacemarkService {
    interface DonationService {
        @GET("/placemarks")
        fun getall(): Call<List<PlacemarkModel>>

        @GET("/placemarks/{id}")
        fun get(@Path("id") id: String): Call<PlacemarkModel>

        @DELETE("/placemarks/{id}")
        fun delete(@Path("id") id: String): Call<PlacemarkWrapper>

        @POST("/placemarks")
        fun post(@Body donation: PlacemarkModel): Call<PlacemarkWrapper>

        @PUT("/donations/{id}")
        fun put(@Path("id") id: String,
                @Body donation: PlacemarkModel
        ): Call<PlacemarkWrapper>
    }
}

object PlacemarkClient {

    val serviceURL = "https://placemark-storee-server.herokuapp.com"

    fun getApi() : PlacemarkService {

        val gson = GsonBuilder().create()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val apiInterface = Retrofit.Builder()
            .baseUrl(serviceURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
        return apiInterface.create(PlacemarkService::class.java)
    }
}