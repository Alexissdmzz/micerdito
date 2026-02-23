package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - AjustesResponse:
 * Esta clase recoge los datos de la API para el fragmento de Ajustes.
 */
data class AjustesResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val id_usuario: String // Id del usuario de la BBDD
)