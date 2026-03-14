package edu.nd.pmcburne.hwapp.one.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// this interface tells retrofit what API endpoints exist
// each function corresponds to one URL to call
interface NcaaApiService {

    // Builds a URL like:
    // https://ncaa-api.henrygd.me/scoreboard/basketball-men/d1/2026/02/17
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScoreboard(
        @Path("gender") gender: String, // "men" or "women"
        @Path("year")   year: String,   // "2026"
        @Path("month")  month: String,  // "02"
        @Path("day")    day: String     // "17"
    ): ScoreboardResponse
}

// a simple object that holds one shared retrofit instance
// only want to create retrofit once
object ApiClient {
    private const val BASE_URL = "https://ncaa-api.henrygd.me/"

    val service: NcaaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // tells Retrofit to use Gson for JSON
            .build()
            .create(NcaaApiService::class.java)
    }
}