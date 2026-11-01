package com.movies.cinetrack.data.remote.api

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException

class TmdbClientTest {
    @Test
    fun `envia autenticacao e consulta discover em portugues com filtros definidos`() = runTest {
        val client = TmdbClient.createHttpClient("test-token").newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                assertEquals("https", request.url.scheme)
                assertEquals("api.themoviedb.org", request.url.host)
                assertEquals("/3/discover/movie", request.url.encodedPath)
                assertEquals("pt-BR", request.url.queryParameter("language"))
                assertEquals("7", request.url.queryParameter("page"))
                assertEquals("false", request.url.queryParameter("include_adult"))
                assertEquals("false", request.url.queryParameter("include_video"))
                assertEquals("popularity.desc", request.url.queryParameter("sort_by"))
                assertEquals("Bearer test-token", request.header("Authorization"))
                assertEquals("application/json", request.header("Accept"))
                chain.respond(
                    """{"page":7,"total_pages":20,"results":[{"id":42,"title":"Filme","overview":null,"poster_path":null,"adult":false}]}""",
                )
            }.build()

        val response = TmdbClient.createApi(client).getDiscoverMovies(page = 7)
        val movie = response.results.single()

        assertEquals(7, response.page)
        assertEquals(20, response.totalPages)
        assertEquals(42, movie.id)
        assertEquals("Filme", movie.title)
        assertNull(movie.overview)
        assertNull(movie.voteAverage)
        assertNull(movie.posterPath)
        assertNull(movie.releaseDate)
    }

    @Test
    fun `preserva erro HTTP para tratamento pelo executor`() = runTest {
        val client = TmdbClient.createHttpClient("test-token").newBuilder()
            .addInterceptor { chain -> chain.respond("{}", code = 401) }
            .build()

        try {
            TmdbClient.createApi(client).getDiscoverMovies()
            fail("Deveria propagar o erro HTTP")
        } catch (error: HttpException) {
            assertEquals(401, error.code())
        }
    }

    @Test
    fun `consulta detalhes pelo id em portugues e interpreta dados opcionais`() = runTest {
        val client = TmdbClient.createHttpClient("test-token").newBuilder()
            .addInterceptor { chain ->
                assertEquals("/3/movie/42", chain.request().url.encodedPath)
                assertEquals("pt-BR", chain.request().url.queryParameter("language"))
                assertEquals("Bearer test-token", chain.request().header("Authorization"))
                chain.respond(
                    """{"id":42,"title":"Filme","overview":null,"runtime":117,"genres":[{"id":18,"name":"Drama"}],"budget":100}""",
                )
            }.build()

        val details = TmdbClient.createApi(client).getMovieDetails(42)

        assertEquals(42, details.id)
        assertEquals(117, details.runtime)
        assertEquals(listOf("Drama"), details.genres?.map { it.name })
        assertNull(details.overview)
        assertNull(details.posterPath)
    }

    @Test
    fun `detalhes inexistentes propagam HTTP 404`() = runTest {
        val client = TmdbClient.createHttpClient("test-token").newBuilder()
            .addInterceptor { chain -> chain.respond("{}", code = 404) }
            .build()

        try {
            TmdbClient.createApi(client).getMovieDetails(42)
            fail("Deveria propagar HTTP 404")
        } catch (error: HttpException) {
            assertEquals(404, error.code())
        }
    }

    @Test
    fun `resposta sem campo obrigatorio propaga erro de desserializacao`() = runTest {
        val client = TmdbClient.createHttpClient("test-token").newBuilder()
            .addInterceptor { chain -> chain.respond("""{"id":42}""") }
            .build()

        try {
            TmdbClient.createApi(client).getMovieDetails(42)
            fail("Deveria propagar o erro de desserialização")
        } catch (_: SerializationException) {
            // A classificação em DataError pertence ao executor.
        }
    }

    @Test
    fun `token vazio ou com espacos e rejeitado antes de criar o cliente`() {
        for (token in listOf("", " ", "Bearer test-token", "test token", "test\ntoken")) {
            assertThrows(IllegalArgumentException::class.java) {
                TmdbClient.createHttpClient(token)
            }
        }
    }

    private fun Interceptor.Chain.respond(json: String, code: Int = 200): Response =
        Response.Builder()
            .request(request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "HTTP $code")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
}
