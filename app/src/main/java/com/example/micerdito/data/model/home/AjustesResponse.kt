package com.example.micerdito.data.model.home

/**
 * Data class que actua como un contenedor de datos, en esta clase recogemos la respuesta del servidor al interactuar con la interfaz de ajustes
 */
data class AjustesResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val id_usuario: String // Id del usuario de la BBDD
)