package com.example.micerdito.data.model.autenticacion

/**
 * Data class que actua como un contenedor de datos, en esta clase recogemos la respuesta del servidor tras un intento de Login
 */

data class LoginResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val user: User? // Objeto que guarda los datos del usuario, usamos ? para en caso de fallar sea Nulo
)