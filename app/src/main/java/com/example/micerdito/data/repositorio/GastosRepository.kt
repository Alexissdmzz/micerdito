package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.Categoria

class GastosRepository {
    private val apiService = RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val response = apiService.getCategorias()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.categorias)
            } else {
                Result.failure(Exception("Error en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}