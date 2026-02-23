package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.AjustesResponse

/**
 * REPOSITORY - AjustesRepository:
 * Esta clase implementa el patrón de diseño Repository, actuando como una capa de abstracción
 * entre el ViewModel y el servicio de API (Retrofit). Su responsabilidad es gestionar las
 * peticiones de red relacionadas con la configuración del perfil del usuario.
 */
class AjustesRepository {
    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Elimina la cuenta del usuario de forma permanente.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @return Result con AjustesResponse en caso de éxito o Exception en caso de fallo.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun eliminarUsuario(idUsuario: String): Result<AjustesResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.deleteUser(idUsuario)

            if (response.isSuccessful && response.body() != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(response.body()!!)
            } else {
                // Encapsulamos la respuesta exitosa del servidor
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
        }
    }
    /**
     * Actualiza el nombre de perfil del usuario en el servidor.
     * @param idUsuario UUID del usuario que realiza el cambio.
     * @param username Nuevo nombre de usuario solicitado.
     * @return Result con la respuesta de éxito o el error capturado.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun editarNombreUsuario(idUsuario: String, username: String): Result<AjustesResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.editUser(idUsuario, username)

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