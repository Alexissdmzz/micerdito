package com.example.micerdito.data.repositorio

import com.example.micerdito.data.conexion.RetrofitClient
import com.example.micerdito.data.model.home.GastosResponse

class GastosRepository {

    private val apiService = RetrofitClient.apiService // Herramienta que nos permite conectar con el servidor

    suspend fun anadirGasto (idUsuario: String, idCategoria: String, titulo: String, importe: Double, fecha: String, descripcion: String): Result<GastosResponse> {
        return try {
            val response = apiService.addGasto(idUsuario, idCategoria, titulo, importe, fecha, descripcion)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}