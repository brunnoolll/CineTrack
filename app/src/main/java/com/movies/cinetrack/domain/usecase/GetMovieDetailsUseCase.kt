package com.movies.cinetrack.domain.usecase

import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.MovieDetails
import com.movies.cinetrack.domain.repository.MovieRepository

class GetMovieDetailsUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): DataResult<MovieDetails> =
        repository.getMovieDetails(movieId)
}