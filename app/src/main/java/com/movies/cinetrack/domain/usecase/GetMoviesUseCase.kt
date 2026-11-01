package com.movies.cinetrack.domain.usecase

import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.repository.MovieRepository

class GetMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): DataResult<List<Movie>> = repository.getMovies()
}