package com.example.micerdito.data.model.autenticacion

/**
 * MODELO DE DATOS - RegisterResponse:
 * Esta clase recoge los datos de la API para el registro del usuario.
 */
data class RegisterResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String // Mensaje informativo que viene del servidor
)