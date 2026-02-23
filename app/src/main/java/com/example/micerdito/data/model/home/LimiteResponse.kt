package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - LimiteResponse:
 * Esta clase recoge los datos de la API de los límites para el fragmento de Home.
 */
data class LimiteResponse(
    val success: Boolean, // Indica si la operación fue exitosa (True) o no (False)
    val message: String // Mensaje informativo que viene del servidor
)