package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.HomeResponse

/**
 * @HomeRepository: actúa como mensajero de la sección de Home de la app. Pide y devuelve datos.
 *
 * @obtenerDatosHome: le pide al servidor los datos necesarios para mostrarlos. Dependiendo de el resultado que de, devuelve true o false (con el error).
 */
class HomeRepository {

    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun obtenerDatosHome(idUsuario: String): Result<HomeResponse> {
        return try {
            val response = apiService.homeUser(idUsuario)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}