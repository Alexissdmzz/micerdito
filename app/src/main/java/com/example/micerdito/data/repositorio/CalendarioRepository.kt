package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.CalendarioResponse
import com.example.micerdito.data.model.home.GastoResponse

/**
 * REPOSITORIO - CalendarioRepository:
 * Esta clase implementa el patrón de diseño Repository, actuando como una capa de abstracción
 * entre el ViewModel y el servicio de API (Retrofit). Su responsabilidad es gestionar las
 * peticiones de red relacionadas con la configuración del perfil del usuario.
 */
class CalendarioRepository {

    private val apiService = RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Obtiene los datos de actividad, fecha de registro y resumen para el gráfico mensual.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @param mes Número del mes a consultar (1-12).
     * @param anio Año a consultar (Ejemplo: 2026).
     * @return Result con CalendarioResponse en caso de éxito o Exception en caso de fallo.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun obtenerDatosCalendario(idUsuario: String, mes: Int, anio: Int): Result<CalendarioResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getDataCalendario(idUsuario, mes, anio)

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
     * Obtiene la lista detallada de gastos realizados en un día específico.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @param anio Año a consultar (Ejemplo: 2026).
     * @param mes Número del mes a consultar (1-12).
     * @param dia Número del día a consultar (1-31).
     * @return Result con CalendarioResponse en caso de éxito o Exception en caso de fallo.
     * Implementa 'suspend' para asegurar que la consulta de los detalles del día
     * se realice de forma asíncrona sin afectar la fluidez de la interfaz.
     */
    suspend fun obtenerGastosPorDia(idUsuario: String, anio: Int, mes: Int, dia: Int): Result<GastoResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getGastosDia(idUsuario, anio, mes, dia)

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