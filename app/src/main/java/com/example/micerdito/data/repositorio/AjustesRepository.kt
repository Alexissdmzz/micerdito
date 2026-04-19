package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.AjustesResponse
import com.example.micerdito.utils.ConexionUtils

/**
 * PATRÓN REPOSITORIO - AjustesRepository
 * Actúa como un mediador estructural entre la capa de presentación (ViewModel)
 * y la fuente de datos remota (Retrofit), encapsulando la lógica de red.
 */
class AjustesRepository {

    // Inyección de la dependencia de red.
    // Al instanciarlo vía Singleton, optimizamos el consumo de memoria.
    private val apiService = RetrofitClient.apiService

    /**
     * Solicita la eliminación permanente de la cuenta de usuario.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @return Result<AjustesResponse> - Patrón de envoltura funcional.
     * que encapsula el estado de éxito o la excepción capturada de forma segura.
     */
    suspend fun eliminarUsuario(idUsuario: String): Result<AjustesResponse> {
        return try {
            // Se suspende la corrutina actual sin bloquear el Main Thread
            // mientras se espera la resolución del socket HTTP.
            val response = apiService.deleteUser(idUsuario)

            // Delegación del mapeo de códigos HTTP (200, 400, 500) a la capa de utilidades
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            // Captura de excepciones de red previniendo cierres forzados
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Solicita la mutación del nombre de perfil del usuario.
     * @param idUsuario UUID del usuario autenticado.
     * @param username Nuevo identificador en texto plano.
     * @return Result<AjustesResponse> con la confirmación de la mutación.
     */
    suspend fun editarNombreUsuario(idUsuario: String, username: String): Result<AjustesResponse> {
        return try {
            val response = apiService.editUser(idUsuario, username)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}