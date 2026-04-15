package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.CalendarioResponse
import com.example.micerdito.data.model.home.GastoResponse
import com.example.micerdito.utils.ConexionUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * REPOSITORIO - CalendarioRepository:
 * Esta clase implementa el patrón de diseño Repository, actuando como una capa de abstracción
 * entre el ViewModel y el servicio de API (Retrofit). Su responsabilidad es gestionar las
 * peticiones de red relacionadas con la configuración del perfil del usuario.
 */
class CalendarioRepository {

    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Obtiene los datos de actividad, fecha de registro y resumen para el gráfico mensual.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @param mes Número del mes a consultar (1-12).
     * @param anio Año a consultar (Ejemplo: 2026).
     * @return Result con CalendarioResponse en caso de éxito o Exception en caso de fallo.
     * Implementa 'suspend' para ejecutarse dentro de una corrutina, asegurando que
     * la petición de red no bloquee el hilo principal.
     */
    suspend fun obtenerDatosCalendario(
        idUsuario: String,
        mes: Int,
        anio: Int
    ): Result<CalendarioResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getDataCalendario(idUsuario, mes, anio)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
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
    suspend fun obtenerGastosPorDia(
        idUsuario: String,
        anio: Int,
        mes: Int,
        dia: Int
    ): Result<GastoResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getGastosDia(idUsuario, anio, mes, dia)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Modifica los datos de un gasto existente, incluyendo la posibilidad de actualizar el ticket.
     * @param idGasto Identificador único (UUID) del gasto a editar.
     * @param titulo Nuevo concepto del gasto.
     * @param importe Valor numérico actualizado.
     * @param descripcion Nota aclaratoria del movimiento.
     * @param fotoActual Referencia al nombre del archivo de imagen actual para evitar su borrado en BBDD.
     * @param fotoTicket Archivo físico de la imagen preparado para subida multipart (opcional).
     * @return Result con GastoResponse confirmando el resultado de la operación.
     */
    suspend fun editarGastos(
        idUsuario: RequestBody,
        idGasto: RequestBody,
        titulo: RequestBody,
        importe: RequestBody,
        descripcion: RequestBody,
        fotoActual: RequestBody,
        fotoTicket: MultipartBody.Part?
    ): Result<GastoResponse> {
        return try {
            // Se incluye el parámetro fotoActual en la llamada a Retrofit
            val response =
                apiService.editGasto(idUsuario, idGasto, titulo, importe, descripcion, fotoActual, fotoTicket)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Solicita al servidor la eliminación permanente de un registro de gasto.
     * @param idGasto Identificador único (UUID) del gasto que se desea borrar.
     * @return Result con GastoResponse que contiene el mensaje de confirmación del borrado.
     * La función es 'suspend' para garantizar que la operación de eliminación no
     * interfiera con el rendimiento de la interfaz de usuario.
     */
    suspend fun deleteGastos(idUsuario: String, idGasto: String): Result<GastoResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.deleteGasto(idUsuario, idGasto)

            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}