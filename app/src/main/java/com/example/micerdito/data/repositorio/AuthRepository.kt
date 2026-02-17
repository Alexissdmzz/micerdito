package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse

/**
 * @AuthRepository: actúa como mensajero de la sección de inicio de sesión y registro de usuario de la app. Pide y devuelve datos.
 *
 * @login: le pide al servidor que inicie sesión, dependiendo de el resultado que de, devuelve true o false (con el error).
 * @register: le pide al servidor que registre al usuario, dependiendo de el resultado que de, devuelve true o false (con el error).
 */
class AuthRepository {
    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = apiService.loginUser(email, pass)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el registro: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recuperarPregunta(email: String): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.getPregunta(email)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarPwd(
        email: String,
        res: String,
        nueva: String
    ): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.cambiarPwd(email, res, nueva)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cambiar la contraseña"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}