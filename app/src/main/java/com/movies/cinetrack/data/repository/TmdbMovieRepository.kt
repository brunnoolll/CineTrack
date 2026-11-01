package com.movies.cinetrack.data.repository

import com.movies.cinetrack.data.mapper.toDomain
import com.movies.cinetrack.data.remote.api.TmdbApi
import com.movies.cinetrack.data.remote.api.ApiRequestExecutor
import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.repository.MovieRepository
import kotlin.random.Random

class TmdbMovieRepository(
    private val api: TmdbApi,
    private val apiRequestExecutor: ApiRequestExecutor,
) : MovieRepository {
    override suspend fun getMovies(): DataResult<List<Movie>> =
        apiRequestExecutor.execute("get_movies") {
            val page = Random.nextInt(1, 21)
            val selection = api.getDiscoverMovies(page = page)

            if (selection.page != page) {
                return@execute DataResult.Failed(DataError.UNKNOWN)
            }

            DataResult.Success(
                selection.results.map { it.toDomain() }
            )
        }

    override suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails> {
        if (movieId <= 0) return DataResult.Failed(DataError.NOT_FOUND)
        return apiRequestExecutor.execute("get_movie_details") {
            val response = api.getMovieDetails(movieId)
            if (response.id != movieId) DataResult.Failed(DataError.UNKNOWN)
            else DataResult.Success(response.toDomain())
        }
    }
}