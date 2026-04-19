package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.utils.ConexionUtils

/**
 * PATRÓN REPOSITORIO - AuthRepository
 * Clase centralizada para la gestión de identidad y acceso de usuarios.
 * Separa la lógica de red de la lógica de presentación, actuando como única fuente
 * de la verdad para las operaciones de sesión.
 * * Nota de concurrencia: Todas las operaciones de esta clase utilizan el modificador 'suspend'
 * para delegar su ejecución a las corrutinas de Kotlin, garantizando que el hilo principal
 * de la interfaz gráfica nunca se bloquee durante las transacciones HTTP.
 */
class AuthRepository {

    // Inyección de la dependencia de red
    private val apiService = RetrofitClient.apiService

    /**
     * Autentica un usuario existente en el sistema.
     * @param email Correo electrónico del usuario.
     * @param pass Contraseña en texto plano para validación contra hash en el servidor.
     * @return Result encapsulando la respuesta del servidor o la excepción capturada.
     */
    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = apiService.loginUser(email, pass)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Registra una nueva identidad en el sistema.
     * @param username Nombre público del usuario.
     * @param email Credencial principal de contacto.
     * @param pwd Contraseña solicitada.
     * @param repeatPwd Confirmación de la contraseña.
     * @param id Identificador de la pregunta de seguridad seleccionada.
     * @param res Respuesta a la pregunta de seguridad (se almacenará cifrada en el servidor).
     * @return Result con la confirmación de la operación.
     */
    suspend fun register(
        username: String,
        email: String,
        pwd: String,
        repeatPwd: String,
        id: Int,
        res: String
    ): Result<RegisterResponse> {
        return try {
            val response = apiService.registerUser(username, email, pwd, repeatPwd, id, res)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Consulta el desafío de seguridad asociado a una cuenta para iniciar la recuperación.
     * @param email Correo asociado a la cuenta a recuperar.
     * @return Result con la pregunta formulada por el usuario.
     */
    suspend fun recuperarPregunta(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.getPregunta(email)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Procesa el cambio de credenciales tras superar el desafío de seguridad.
     * @param email Correo de la cuenta afectada.
     * @param res Respuesta de validación proporcionada por el usuario.
     * @param nueva Nueva contraseña a establecer.
     * @return Result confirmando el estado de la actualización.
     */
    suspend fun actualizarPwd(
        email: String,
        res: String,
        nueva: String
    ): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.cambiarPwd(email, res, nueva)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}