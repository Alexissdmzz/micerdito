package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - GastosResponse:
 * Esta clase recoge los datos de la API de las Categorias para el fragmento ....
 */
data class GastosResponse(
    val success: Boolean, // Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val id_gasto: String? // Recoge el id del gasto, es null si falla algo
)