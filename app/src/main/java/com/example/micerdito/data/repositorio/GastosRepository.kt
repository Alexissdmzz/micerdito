package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.model.home.GastosResponse

class GastosRepository {
    private val apiService =
        RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val response = apiService.getCategorias()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.categorias)
            } else {
                Result.failure(Exception("Error de conexión: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertarGasto(
        idUsuario: String,
        idCategoria: String,
        titulo: String,
        importe: Double,
        fechaGasto: String,
        descripcion: String
    ): Result<GastosResponse> {
        return try {
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
                Result.success(body)
            } else {
                Result.failure(Exception("Error de conexión: ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}