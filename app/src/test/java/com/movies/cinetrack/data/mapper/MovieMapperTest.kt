package com.movies.cinetrack.data.mapper

import com.movies.cinetrack.data.remote.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {
    @Test
    fun `preserva os dados do filme no dominio`() {
        val movie = MovieDto(
            id = 42,
            title = "Filme",
            overview = "Sinopse",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            voteAverage = 7.8,
            releaseDate = "2026-10-31",
        ).toDomain()

        assertEquals(42, movie.id)
        assertEquals("Filme", movie.title)
        assertEquals("Sinopse", movie.overview)
        assertEquals("/poster.jpg", movie.posterPath)
        assertEquals("/backdrop.jpg", movie.backdropPath)
        assertEquals(7.8, movie.voteAverage, 0.0)
        assertEquals("2026-10-31", movie.releaseDate)
    }

    @Test
    fun `campos opcionais ausentes recebem os valores padrao do dominio`() {
        val movie = MovieDto(id = 42, title = "Filme").toDomain()

        assertEquals("", movie.overview)
        assertEquals(0.0, movie.voteAverage, 0.0)
        assertNull(movie.posterPath)
        assertNull(movie.backdropPath)
        assertNull(movie.releaseDate)
    }

    @Test
    fun `caminhos de imagem e data em branco sao tratados como ausentes`() {
        for (blank in listOf("", " ", "\t\n")) {
            val movie = MovieDto(42, "Filme", posterPath = blank, backdropPath = blank, releaseDate = blank).toDomain()

            assertNull(movie.posterPath)
            assertNull(movie.backdropPath)
            assertNull(movie.releaseDate)
        }
    }
}
