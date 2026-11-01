package com.movies.cinetrack.data.remote.api

import com.movies.cinetrack.data.remote.dto.MoviesResponse
import com.movies.cinetrack.data.remote.dto.MovieDetailsDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface TmdbApi {
    @GET("discover/movie")
    suspend fun getDiscoverMovies(
        @Query("language") language: String = "pt-BR",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("sort_by") sortBy: String = "popularity.desc",
    ): MoviesResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "pt-BR",
    ): MovieDetailsDto
}