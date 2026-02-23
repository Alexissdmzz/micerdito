package com.example.micerdito.data.model.autenticacion

/**
 * MODELO DE DATOS - ForgotPasswordResponse:
 * Esta clase recoge los datos de la API para la recuperación de contraseña.
 */
data class ForgotPasswordResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String?, // Mensaje informativo que viene del servidor
    val pregunta: String? = null // Objeto que guarda la pregunta del usuario, usamos ? para en caso de fallar sea Nulo
)