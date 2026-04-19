package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.model.home.GastoResponse
import com.example.micerdito.utils.ConexionUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * PATRÓN REPOSITORIO - GastosRepository
 * Gestiona las operaciones de dominio relacionadas con la creación de transacciones
 * y la consulta del catálogo de clasificación.
 * * Nota de concurrencia: Todas las operaciones de esta clase utilizan el modificador 'suspend'
 * para delegar su ejecución a las corrutinas de Kotlin, garantizando que el hilo principal
 * nunca se bloquee durante el I/O de red o la lectura de archivos del disco local.
 */
class GastosRepository {

    // Inyección de la dependencia de red
    private val apiService = RetrofitClient.apiService

    /**
     * Recupera el catálogo maestro de categorías disponibles.
     * @return Result<List<Categoria>> Colección de entidades de dominio listas para la UI.
     */
    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val response = apiService.getCategorias()
            val resultado = ConexionUtils.procesarRespuesta(response)

            // Uso del operador funcional 'fold' para extraer y mapear la lista interna
            // del DTO CategoriaResponse, entregando a la UI estrictamente lo que necesita.
            resultado.fold(
                onSuccess = { categoriasResponse ->
                    Result.success(categoriasResponse.categorias)
                },
                onFailure = {
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }

    /**
     * Persiste una nueva transacción financiera, empaquetando opcionalmente comprobantes multimedia.
     * @param idUsuario UUID del propietario.
     * @param idCategoria UUID de la categoría asignada.
     * @param titulo Concepto de la transacción.
     * @param importe Magnitud financiera.
     * @param fechaGasto Fecha de la operación (Formato ISO / YYYY-MM-DD).
     * @param descripcion Notas adicionales.
     * @param fotoRuta Ruta absoluta en el almacenamiento local de la imagen adjunta (Nullable).
     * @return Result<GastoResponse> Confirmación de la persistencia en el backend.
     */
    suspend fun insertarGasto(
        idUsuario: String,
        idCategoria: String,
        titulo: String,
        importe: Double,
        fechaGasto: String,
        descripcion: String,
        fotoRuta: String?
    ): Result<GastoResponse> {
        return try {
            // Transformación de tipos primitivos a bloques MIME (Multipart) aislando
            // a la capa de presentación de la lógica estricta de OkHttp.
            val idUserBody =
                idUsuario.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val idCatBody =
                idCategoria.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val tituloBody = titulo.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val importeBody =
                importe.toString().toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val fechaBody =
                fechaGasto.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val descBody =
                descripcion.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

            // Ensamblaje seguro del payload binario (Imagen)
            var fotoPart: MultipartBody.Part? = null
            fotoRuta?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                } else {
                    // Failsafe preventivo: Aborta la petición y avisa si el puntero al archivo es inválido
                    return Result.failure(Exception("Error de I/O: No se encontró la imagen en la ruta especificada."))
                }
            }

            val response = apiService.insertGasto(
                idUserBody, idCatBody, tituloBody, importeBody, fechaBody, descBody, fotoPart
            )

            ConexionUtils.procesarRespuesta(response)

        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}