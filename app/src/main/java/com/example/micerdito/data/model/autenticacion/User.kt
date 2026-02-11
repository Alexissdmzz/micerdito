package com.example.micerdito.data.model.autenticacion

/**
 * Data class que actua como un contenedor de datos, en esta clase recogemos los datos del usuario ya existente
 */

data class User(
    val id: String, // Id del usuario en la BBDD
    val username: String, // Nombre del usuario en la BBDD
    val email: String // Correo del usuario en la BBDD
)