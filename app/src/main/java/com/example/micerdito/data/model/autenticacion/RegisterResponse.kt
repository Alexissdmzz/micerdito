package com.example.micerdito.data.model.autenticacion

/**
 * DTO (Data Transfer Object) - RegisterResponse
 * Modelo de datos inmutable encargado de deserializar la carga útil (payload) devuelta por la API
 * durante el proceso de registro de un nuevo usuario. Actúa como puente entre la capa de red y el Repositorio.
 */
data class RegisterResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de registro)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor (ej. formato inválido, cuenta ya existente, etc.)
    val message: String
)