package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.model.home.LimiteResponse
import com.example.micerdito.data.model.home.MovimientosResponse

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

            if (response.isSuccessful && response.body() != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(response.body()!!)
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
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

            if (response.isSuccessful && response.body() != null) {
                val graficoResponse = response.body()!!

                // Verificamos si la lógica del servidor (PHP) fue exitosa
                if (graficoResponse.success) {
                    // Devolvemos solo la lista
                    Result.success(graficoResponse.listaGrafico)
                } else {
                    Result.failure(Exception("Error lógico: Mensaje del servidor"))
                }
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
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

            if (response.isSuccessful && response.body() != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(response.body()!!)
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
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
            if (response.isSuccessful && response.body() != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(response.body()!!)
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
        }
    }
}