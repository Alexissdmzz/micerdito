package com.example.micerdito.utils

import android.util.Log
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

    // TAG de clase: identifica la fuente del log sin revelar lógica interna
    private const val TAG = "ConexionUtils"

    /**
     * Evalúa el código de estado de una transacción HTTP y desempaqueta el contenido.
     * @param response Objeto contenedor de la respuesta de la capa de red.
     * @return Result con el tipo de dato genérico o una excepción con un mensaje descriptivo.
     */
    fun <T> procesarRespuesta(response: Response<T>): Result<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    // Log interno: se registra el código real para trazabilidad
                    Log.w(TAG, "Respuesta exitosa pero body nulo. Código: ${response.code()}")
                    Result.failure(Exception("La respuesta del servidor está vacía"))
                }
            } else {
                val mensaje = when (response.code()) {
                    400 -> "Solicitud incorrecta"
                    401 -> "No autorizado. Inicia sesión de nuevo"
                    403 -> "Acceso denegado"
                    404 -> "Servidor temporalmente no disponible"
                    500 -> "Error interno del servidor"
                    505 -> "Versión de protocolo no compatible"
                    else -> "Error en el servidor"
                }

                Result.failure(Exception(mensaje))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al procesar respuesta HTTP", e)
            Result.failure(Exception("No se ha podido completar la operación"))
        }
    }

    /**
     * Intercepta errores a nivel de conectividad o hardware y los traduce
     * a mensajes comprensibles para el usuario final.
     * @param e Excepción capturada durante la petición de red.
     * @return Result tipado como fallo.
     */
    fun manejarExcepcion(e: Exception): Result<Nothing> {
        return when (e) {
            is SocketTimeoutException -> {
                Log.w(TAG, "Timeout de red alcanzado", e)
                Result.failure(Exception("Tiempo de espera agotado. Inténtalo de nuevo"))
            }

            is IOException -> {
                Log.w(TAG, "Error de E/S de red", e)
                Result.failure(Exception("No se pudo conectar. Revisa tu conexión"))
            }

            else -> {
                Log.e(TAG, "Excepción de red no controlada: ${e::class.simpleName}", e)
                Result.failure(Exception("Se ha producido un error inesperado"))
            }
        }
    }
}