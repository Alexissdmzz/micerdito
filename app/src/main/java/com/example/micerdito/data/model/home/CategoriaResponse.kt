package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - CategoriaResponse:
 * Esta clase recoge los datos de la API de las Categorias para el fragmento de Añadir Gasto.
 */
data class CategoriaResponse(
    val success: Boolean, // Indica si la operación fue exitosa (True) o no (False)
    val categorias: List<Categoria>, // Recoge la lista con todos los campos de @Categoria
    val message: String? = null // Mensaje informativo que viene del servidor
)