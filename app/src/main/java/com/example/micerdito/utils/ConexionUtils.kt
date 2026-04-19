package com.example.micerdito.utils

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * UTILIDAD - ConexionUtils
 * Componente transversal de apoyo.
 * Centraliza la lógica de evaluación de respuestas HTTP y la gestión de excepciones de red,
 * estandarizando la salida de datos mediante el encapsulamiento en objetos Result.
 */
object ConexionUtils {

    /**
     * Evalúa el código de estado de una transacción HTTP y desempaqueta el contenido.
     * * @param response Objeto contenedor de la respuesta de la capa de red.
     * @return Result con el tipo de dato genérico o una excepción con un mensaje descriptivo.
     */
    fun <T> procesarRespuesta(response: Response<T>): Result<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("La respuesta del servidor está vacía"))
                }
            } else {
                val mensaje = when (response.code()) {
                    400 -> "Solicitud incorrecta"
                    401 -> "No autorizado. Inicia sesión de nuevo"
                    403 -> "Acceso denegado"
                    404 -> "Recurso no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error en el servidor (${response.code()})"
                }

                Result.failure(Exception(mensaje))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al procesar la respuesta del servidor"))
        }
    }

    /**
     * Intercepta errores a nivel de conectividad o hardware y los traduce
     * a mensajes comprensibles para el usuario final.
     * * @param e Excepción capturada durante la petición de red.
     * @return Result tipado como fallo.
     */
    fun manejarExcepcion(e: Exception): Result<Nothing> {
        return when (e) {
            is SocketTimeoutException ->
                Result.failure(Exception("Tiempo de espera agotado. Inténtalo de nuevo"))

            is IOException ->
                Result.failure(Exception("No se pudo conectar con el servidor. Revisa tu conexión"))

            else ->
                Result.failure(Exception("Se ha producido un error inesperado"))
        }
    }
}