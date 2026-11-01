package com.movies.cinetrack.data.remote.api

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object TmdbClient {
    fun createHttpClient(readAccessToken: String): OkHttpClient {
        require(readAccessToken.isNotBlank() && readAccessToken.none { it.isWhitespace() }) {
            "Informe o API Read Access Token da TMDB sem o prefixo Bearer."
        }
        return OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $readAccessToken")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun createApi(client: OkHttpClient): TmdbApi = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(client)
        .addConverterFactory(
            Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create(TmdbApi::class.java)
}