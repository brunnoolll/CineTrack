package com.movies.cinetrack.data.mapper

import com.movies.cinetrack.data.remote.dto.GenreDto
import com.movies.cinetrack.data.remote.dto.MovieDetailsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailsMapperTest {
    @Test
    fun `mapeia filme completo e preserva os dados comuns`() {
        val details = MovieDetailsDto(
            id = 42,
            title = "Filme",
            overview = "Sinopse",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            voteAverage = 7.8,
            releaseDate = "2026-10-31",
            runtime = 117,
            genres = listOf(GenreDto("Drama"), GenreDto("Comédia")),
        ).toDomain()

        assertEquals(42, details.movie.id)
        assertEquals("Filme", details.movie.title)
        assertEquals("Sinopse", details.movie.overview)
        assertEquals("/poster.jpg", details.movie.posterPath)
        assertEquals("/backdrop.jpg", details.movie.backdropPath)
        assertEquals(7.8, details.movie.voteAverage, 0.0)
        assertEquals("2026-10-31", details.movie.releaseDate)
        assertEquals(117, details.runtimeMinutes)
        assertEquals(listOf("Drama", "Comédia"), details.genres)
    }

    @Test
    fun `duracao ausente ou nao positiva e tratada como desconhecida`() {
        for (runtime in listOf(null, 0, -1)) {
            val details = MovieDetailsDto(42, "Filme", runtime = runtime).toDomain()
            assertNull(details.runtimeMinutes)
        }
    }

    @Test
    fun `generos sao normalizados sem entradas vazias ou repetidas`() {
        val details = MovieDetailsDto(
            42, "Filme",
            genres = listOf(GenreDto(null), GenreDto(" "), GenreDto(" Drama "), GenreDto("Drama"), GenreDto(" Comédia ")),
        ).toDomain()

        assertEquals(listOf("Drama", "Comédia"), details.genres)
    }

    @Test
    fun `generos ausentes ou vazios produzem lista vazia`() {
        assertEquals(emptyList<String>(), MovieDetailsDto(42, "Filme").toDomain().genres)
        assertEquals(emptyList<String>(), MovieDetailsDto(42, "Filme", genres = emptyList()).toDomain().genres)
    }

    @Test
    fun `detalhes sem dados opcionais preservam os valores padrao do filme`() {
        val details = MovieDetailsDto(42, "Filme").toDomain()

        assertEquals("", details.movie.overview)
        assertEquals(0.0, details.movie.voteAverage, 0.0)
        assertNull(details.movie.posterPath)
        assertNull(details.movie.backdropPath)
        assertNull(details.movie.releaseDate)
    }

    @Test
    fun `detalhes com imagens e data em branco nao expoem esses valores`() {
        val details = MovieDetailsDto(42, "Filme", posterPath = " ", backdropPath = "", releaseDate = "\t").toDomain()

        assertNull(details.movie.posterPath)
        assertNull(details.movie.backdropPath)
        assertNull(details.movie.releaseDate)
    }
}