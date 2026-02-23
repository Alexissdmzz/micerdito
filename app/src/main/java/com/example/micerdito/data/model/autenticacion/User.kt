package com.example.micerdito.data.model.autenticacion

/**
 * MODELO DE DATOS - User:
 * Esta clase recoge los datos de la API para el usuario ya existente.
 */
data class User(
    val id: String, // Id del usuario en la BBDD
    val username: String, // Nombre del usuario en la BBDD
    val email: String // Correo del usuario en la BBDD
)