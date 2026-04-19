package com.example.micerdito.data.model.autenticacion

/**
 * DTO (Data Transfer Object) - LoginResponse
 * Modelo de datos inmutable encargado de deserializar la carga útil (payload) devuelta por la API
 * durante el proceso de autenticación. Actúa como puente entre la capa de red (Retrofit) y el Repositorio.
 */
data class LoginResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de autenticación)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor (ej. credenciales inválidas, cuenta bloqueada, etc.)
    val message: String,

    // Entidad de dominio anidada. Se define como Nullable (User?) para garantizar
    // la seguridad de memoria en caso de que el login falle y el backend omita este nodo.
    val user: User?
)