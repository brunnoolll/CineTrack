package com.movies.cinetrack.presentation

import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.repository.MovieRepository

internal class OnlineRepositoryFake : MovieRepository {
    val movie = Movie(42, "Filme", "Sinopse", null, null, 7.8, null)
    val details = MovieDetails(movie, 117, listOf("Drama"))
    var calls = 0
    val ids = mutableListOf<Int>()
    var moviesResponse: suspend () -> DataResult<List<Movie>> = { DataResult.Success(listOf(movie)) }
    var detailsResponse: suspend () -> DataResult<MovieDetails> = { DataResult.Success(details) }
    override suspend fun getMovies(): DataResult<List<Movie>> {
        calls++
        return moviesResponse()
    }
    override suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails> {
        calls++
        ids += movieId
        return detailsResponse()
    }
}