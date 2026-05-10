package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.CalendarioResponse
import com.example.micerdito.data.model.home.GastoResponse
import com.example.micerdito.utils.ConexionUtils
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * PATRÓN REPOSITORIO - CalendarioRepository
 * Capa de abstracción encargada de orquestar las operaciones de red vinculadas
 * al historial temporal del usuario (Calendario) y la gestión individual de transacciones.
 * * * Nota de concurrencia: Todas las operaciones de esta clase utilizan el modificador 'suspend'
 * para delegar su ejecución a las corrutinas de Kotlin, garantizando que el hilo principal
 * de la interfaz gráfica nunca se bloquee durante las transacciones HTTP o subidas de archivos.
 */
class CalendarioRepository {

    // Inyección de la dependencia de red
    private val apiService = RetrofitClient.apiService

    /**
     * Obtiene la carga inicial de datos agregados (Multi-Result Set) para renderizar
     * el calendario interactivo y sus gráficas asociadas.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @param mes Número del mes a consultar (1-12).
     * @param anio Año a consultar (Ejemplo: 2026).
     * @return Result con la información agregada o excepción capturada.
     */
    suspend fun obtenerDatosCalendario(
        idUsuario: String,
        mes: Int,
        anio: Int
    ): Result<CalendarioResponse> {
        return try {
            val response = apiService.getDataCalendario(idUsuario, mes, anio)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Consulta el desglose granular de transacciones asociadas a una fecha específica.
     * @param idUsuario Identificador único (UUID) del usuario.
     * @param anio Año a consultar.
     * @param mes Número del mes a consultar.
     * @param dia Día exacto del mes (1-31).
     * @return Result con la colección de gastos de la jornada.
     */
    suspend fun obtenerGastosPorDia(
        idUsuario: String,
        anio: Int,
        mes: Int,
        dia: Int
    ): Result<GastoResponse> {
        return try {
            val response = apiService.getGastosDia(idUsuario, anio, mes, dia)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Mutación compleja (Multipart) para actualizar los metadatos y/o el comprobante multimedia de un gasto.
     * @param idUsuario Identificador único del propietario (RequestBody).
     * @param idGasto Identificador único (UUID) del registro a modificar.
     * @param titulo Concepto de la transacción.
     * @param importe Magnitud financiera actualizada.
     * @param descripcion Notas adicionales adjuntas al movimiento.
     * @param fotoActual Referencia en texto de la imagen preexistente para evitar orfandad en el servidor.
     * @param fotoTicket Carga útil binaria (Archivo de imagen) para sobreescritura (Nullable).
     * @return Result confirmando el estado de la actualización en el servidor.
     */
    suspend fun editarGastos(
        idUsuario: RequestBody,
        idGasto: RequestBody,
        titulo: RequestBody,
        importe: RequestBody,
        descripcion: RequestBody,
        fotoActual: RequestBody,
        fechaGasto: RequestBody,
        fotoTicket: MultipartBody.Part?
    ): Result<GastoResponse> {
        return try {
            val response = apiService.editGasto(
                idUsuario, idGasto, titulo, importe, descripcion, fotoActual, fechaGasto, fotoTicket
            )
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Ejecuta una operación destructiva (Soft/Hard Delete) sobre un registro financiero.
     * @param idUsuario Identificador del propietario para validación de seguridad.
     * @param idGasto Identificador único (UUID) del movimiento a purgar.
     * @return Result con el feedback del servidor sobre la eliminación.
     */
    suspend fun deleteGastos(idUsuario: String, idGasto: String): Result<GastoResponse> {
        return try {
            val response = apiService.deleteGasto(idUsuario, idGasto)
            ConexionUtils.procesarRespuesta(response)
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}