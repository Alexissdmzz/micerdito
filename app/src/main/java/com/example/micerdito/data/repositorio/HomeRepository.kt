package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.model.home.LimiteResponse
import com.example.micerdito.data.model.home.MovimientosResponse
import com.example.micerdito.utils.ConexionUtils

/**
 * REPOSITORIO - HomeRepository
 * Este repositorio centraliza la lógica de obtención de datos para la pantalla principal.
 * Gestiona la sincronización entre el estado financiero del usuario (gastos totales vs. límite)
 * y el listado de actividad reciente.
 */
class HomeRepository {

    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Recupera los datos estadísticos generales del usuario para el mes actual.
     * @param idUsuario UUID del usuario autenticado.
     * @return Result<HomeResponse>: Contiene el nombre del usuario, total gastado,
     * límite configurado y el nombre del mes actual.
     */
    suspend fun obtenerDatosHome(idUsuario: String): Result<HomeResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.homeUser(idUsuario)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Recupera el desglose de gastos por categoría del mes actual para representar en el gráfico.
     * @param idUsuario ID del usuario autenticado.
     * @return Result<List<GastoPorCategoria>>: Una lista de objetos que contienen el nombre de
     * la categoría, el sumatorio de sus gastos y su color representativo.
     */
    suspend fun obtenerGastosPorCategoria(idUsuario: String): Result<List<GastoPorCategoria>> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.obtenerGastosGrafico(idUsuario)

            val resultado = ConexionUtils.procesarRespuesta(response)

            resultado.fold(
                onSuccess = { graficoResponse ->
                    if (graficoResponse.success) {
                        Result.success(graficoResponse.listaGrafico)
                    } else {
                        Result.failure(Exception("No se pudieron obtener los datos del gráfico"))
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
     * Obtiene la lista de los últimos movimientos (gastos) registrados por el usuario.
     * @param idUsuario UUID del usuario.
     * @return Result<MovimientosResponse>: Incluye una lista de gastos con sus
     * respectivas categorías, iconos y colores asociados.
     */
    suspend fun obtenerMovimientosRecientes(idUsuario: String): Result<MovimientosResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.homeMoves(idUsuario)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Registra o actualiza el límite de gasto mensual del usuario.
     * @param idUsuario UUID del usuario.
     * @param limite Valor numérico (Double) que representa el nuevo tope de gasto.
     * @return Result<LimiteResponse>: Confirmación de la actualización en la base de datos.
     */
    suspend fun guardarLimite(idUsuario: String, limite: Double): Result<LimiteResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.homeLimit(idUsuario, limite)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}