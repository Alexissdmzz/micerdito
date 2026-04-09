package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.utils.ConexionUtils

/**
 * REPOSITORIO - AuthRepository
 * Clase centralizada para la gestión de autenticación. Implementa el patrón Repository para
 * separar la lógica de obtención de datos (Retrofit) de la lógica de presentación (ViewModels).
 */
class AuthRepository {
    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Gestiona el inicio de sesión de usuarios.
     * @param email Correo electrónico del usuario.
     * @param pass Contraseña en texto plano (será validada contra hash en el servidor).
     * @return Result encapsulando la respuesta de éxito o excepción de red.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.loginUser(email, pass)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * @param id Identificador de la pregunta de seguridad seleccionada.
     * @param res Respuesta a la pregunta de seguridad (se almacenará como hash en BD).
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
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
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.registerUser(username, email, pwd, repeatPwd, id, res)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Recupera la pregunta del usuario almacenada en la BBDD.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun recuperarPregunta(email: String): Result<ForgotPasswordResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getPregunta(email)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Actualiza una nueva contraseña para el usuario.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun actualizarPwd(
        email: String,
        res: String,
        nueva: String
    ): Result<ForgotPasswordResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.cambiarPwd(email, res, nueva)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}