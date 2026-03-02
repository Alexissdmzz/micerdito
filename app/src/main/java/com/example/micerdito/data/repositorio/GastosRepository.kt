package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.model.home.GastoResponse

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
            val body = response.body()

            if (response.isSuccessful && body != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(body.categorias)
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error de conexión: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
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
     * @return Result<GastosResponse>: Objeto que confirma si la inserción fue exitosa en la BD.
     */
    suspend fun insertarGasto(
        idUsuario: String,
        idCategoria: String,
        titulo: String,
        importe: Double,
        fechaGasto: String,
        descripcion: String
    ): Result<GastoResponse> {
        return try {
            // Ejecución de la llamada síncrona dentro del contexto de la corrutina
            val response = apiService.insertGasto(
                idUsuario,
                idCategoria,
                titulo,
                importe,
                fechaGasto,
                descripcion
            )

            val body = response.body()
            if (response.isSuccessful && body != null) {
                // Encapsulamos la respuesta exitosa del servidor
                Result.success(body)
            } else {
                // Manejo de errores de respuesta del servidor
                Result.failure(Exception("Error de conexión: ${response.code()}"))
            }

        } catch (e: Exception) {
            // Manejo de errores de red
            Result.failure(e)
        }
    }
}