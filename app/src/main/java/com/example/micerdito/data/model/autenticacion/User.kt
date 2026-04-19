package com.example.micerdito.data.model.autenticacion

/**
 * DTO (Data Transfer Object) - Entidad User
 * Representa el modelo de dominio principal del usuario dentro de la capa de cliente.
 * Se utiliza como carga útil (payload) anidada en las respuestas de autenticación exitosas.
 */
data class User(
    // Identificador único universal (UUID) generado por el servidor para la gestión de relaciones
    val id: String,

    // Nombre de visualización utilizado para personalizar la capa de presentación (UI)
    val username: String,

    // Credencial principal de contacto y validación de identidad
    val email: String
)