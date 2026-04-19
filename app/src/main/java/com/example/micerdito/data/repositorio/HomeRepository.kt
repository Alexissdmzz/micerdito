package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.model.home.LimiteResponse
import com.example.micerdito.data.model.home.MovimientosResponse
import com.example.micerdito.utils.ConexionUtils

/**
 * PATRÓN REPOSITORIO - HomeRepository
 * Repositorio central (Facade) que orquesta la obtención de datos agregados para el Dashboard.
 * Gestiona la sincronización entre el estado financiero global, la visualización gráfica
 * y el histórico reciente de transacciones.
 * * Nota de concurrencia: Todas las operaciones de esta clase utilizan el modificador 'suspend'
 * para delegar su ejecución a las corrutinas de Kotlin, garantizando que el hilo principal
 * (UI Thread) permanezca fluido y reactivo durante las peticiones asíncronas.
 */
class HomeRepository {

    // Inyección de la dependencia de red (Remote Data Source)
    private val apiService = RetrofitClient.apiService

    /**
     * Recupera el payload agregado (Aggregated Payload) con las métricas financieras principales.
     * @param idUsuario UUID del usuario autenticado.
     * @return Result<HomeResponse> Envoltorio con el total gastado, límite y metadatos del mes.
     */
    suspend fun obtenerDatosHome(idUsuario: String): Result<HomeResponse> {
        return try {
            val response = apiService.homeUser(idUsuario)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Consulta y desempaqueta la estructura de datos necesaria para renderizar el gráfico circular.
     * @param idUsuario UUID del usuario autenticado.
     * @return Result<List<GastoPorCategoria>> Colección de métricas agrupadas, lista para la UI.
     */
    suspend fun obtenerGastosPorCategoria(idUsuario: String): Result<List<GastoPorCategoria>> {
        return try {
            val response = apiService.obtenerGastosGrafico(idUsuario)
            val resultado = ConexionUtils.procesarRespuesta(response)

            // Desempaquetado funcional (Unwrapping) para aislar la capa de presentación
            // del envoltorio de red (GraficoResponse).
            resultado.fold(
                onSuccess = { graficoResponse ->
                    // Failsafe defensivo: Validamos la bandera booleana del servidor antes de inyectar datos
                    if (graficoResponse.success) {
                        Result.success(graficoResponse.listaGrafico)
                    } else {
                        Result.failure(Exception("El servidor denegó la extracción de métricas del gráfico."))
                    }
                },
                onFailure = {
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Obtiene una sub-colección paginada o limitada del histórico de transacciones recientes.
     * @param idUsuario UUID del usuario.
     * @return Result<MovimientosResponse> Envoltorio con la lista de entidades Gasto.
     */
    suspend fun obtenerMovimientosRecientes(idUsuario: String): Result<MovimientosResponse> {
        return try {
            val response = apiService.homeMoves(idUsuario)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Ejecuta una mutación parcial (Update) sobre el perfil financiero del usuario.
     * @param idUsuario UUID del usuario.
     * @param limite Nuevo umbral presupuestario establecido por el usuario.
     * @return Result<LimiteResponse> Confirmación de la mutación persistida en servidor.
     */
    suspend fun guardarLimite(idUsuario: String, limite: Double): Result<LimiteResponse> {
        return try {
            val response = apiService.homeLimit(idUsuario, limite)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}