package com.movies.cinetrack.domain.repository

import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.domain.model.Movie
import com.movies.cinetrack.domain.model.MovieDetails

interface MovieRepository {
    /** Consulta uma nova seleção Discover diretamente na API. */
    suspend fun getMovies(): DataResult<List<Movie>>

    /** Consulta detalhes diretamente na API, sem persistência ou TTL. */
    suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails>
}