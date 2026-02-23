package com.example.micerdito.data.model.autenticacion

/**
 * MODELO DE DATOS - LoginResponse:
 * Esta clase recoge los datos de la API para el Login del usuario.
 */
data class LoginResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val user: User? // Objeto que guarda los datos del usuario, usamos ? para en caso de fallar sea Nulo
)