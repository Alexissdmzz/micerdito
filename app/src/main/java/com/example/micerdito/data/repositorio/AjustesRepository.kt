package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.AjustesResponse

/**
 * @AjustesRepository actúa como mensajero de la sección de ajustes de la app. Pide y devuelve datos.
 *
 * @eliminarUsuario: le pide al servidor que elimine al usuario, dependiendo de el resultado que de, devuelve true o false (con el error).
 * @editarNombreUsuario: le pide al servidor que edite el nombre del usuario, dependiendo de el resultado que de, devuelve true o false (con el error).
 */

class AjustesRepository {

    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun eliminarUsuario(idUsuario: String): Result<AjustesResponse> {
        return try {
            val response = apiService.deleteUser(idUsuario)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarNombreUsuario(idUsuario: String, username: String): Result<AjustesResponse> {
        return try {
            val response = apiService.editUser(idUsuario, username)

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