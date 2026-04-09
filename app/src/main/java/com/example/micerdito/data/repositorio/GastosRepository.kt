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
 * REPOSITORIO - GastosRepository:
 * Gestiona todas las operaciones relacionadas con la creación y clasificación de gastos.
 * Actúa como puente entre la interfaz de usuario (Fragments de inserción) y el backend en PHP.
 */
class GastosRepository {
    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    /**
     * Recupera el catálogo de categorías disponibles desde el servidor.
     * @return Result<List<Categoria>>: Una lista de objetos Categoria que contienen
     * el nombre, icono y color hexadecimal para su representación visual en la App.
     */
    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.getCategorias()
            val resultado = ConexionUtils.procesarRespuesta(response)

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
     * Envía los datos de un nuevo gasto al servidor para su persistencia.
     * @param idUsuario Identificador único del propietario del gasto.
     * @param idCategoria Referencia a la categoría seleccionada (UUID).
     * @param titulo Concepto breve del gasto.
     * @param importe Valor numérico del gasto.
     * @param fechaGasto Fecha de la transacción en formato yyyy-MM-dd.
     * @param descripcion Detalles adicionales opcionales.
     * @param fotoRuta Foto del ticket del gasto.
     * @return Result<GastosResponse>: Objeto que confirma si la inserción fue exitosa en la BD.
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
            // Convertimos los datos de texto a RequestBody para Multipart
            val idUserBody = idUsuario.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val idCatBody = idCategoria.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val tituloBody = titulo.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val importeBody = importe.toString().toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val fechaBody = fechaGasto.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
            val descBody = descripcion.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

            // Preparamos el archivo de la foto si existe la ruta
            var fotoPart: MultipartBody.Part? = null
            fotoRuta?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)
                } else {
                    return Result.failure(Exception("No se encontró la imagen seleccionada"))
                }
            }

            // Ejecución de la llamada
            val response = apiService.insertGasto(
                idUserBody,
                idCatBody,
                tituloBody,
                importeBody,
                fechaBody,
                descBody,
                fotoPart
            )

            ConexionUtils.procesarRespuesta(response)

        } catch (e: Exception) {
            ConexionUtils.manejarExcepcion(e)
        }
    }
}