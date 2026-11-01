package com.movies.cinetrack.data.remote.api

import com.movies.cinetrack.domain.model.DataError
import com.movies.cinetrack.domain.model.DataResult
import com.movies.cinetrack.core.diagnostics.Logger
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Registra falhas de consultas à API e converte erros conhecidos em DataError.
 * Cancelamentos e exceções inesperadas são propagados para o chamador.
 * Use apenas para operações remotas: IOException é interpretada como falha de rede.
 */
class ApiRequestExecutor @Inject constructor(private val logger: Logger) {
    suspend fun <T> execute(operation: String, block: suspend () -> DataResult<T>): DataResult<T> = try {
        block().also { result ->
            if (result is DataResult.Failed) {
                logger.error("A operação '$operation' retornou uma falha: ${result.reason.name}.")
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: HttpException) {
        val reason = when (error.code()) {
            401, 403 -> DataError.UNAUTHORIZED
            404 -> DataError.NOT_FOUND
            429 -> DataError.RATE_LIMITED
            in 500..599 -> DataError.SERVER
            else -> DataError.UNKNOWN
        }
        logger.error("Falha na resposta da API durante '$operation': HTTP ${error.code()}, categoria ${reason.name}.", error)
        DataResult.Failed(reason)
    } catch (error: IOException) {
        logger.error("Falha de rede durante '$operation': não foi possível concluir a comunicação com a API.", error)
        DataResult.Failed(DataError.NETWORK)
    } catch (error: SerializationException) {
        logger.error("Falha ao interpretar a resposta da API durante '$operation': formato de dados inválido.", error)
        DataResult.Failed(DataError.UNKNOWN)
    }
}