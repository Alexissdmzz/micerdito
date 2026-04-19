package com.example.micerdito.data.model.autenticacion

/**
 * DTO (Data Transfer Object) - ForgotPasswordResponse
 * Modelo de datos inmutable utilizado por Retrofit/Gson para deserializar la respuesta
 * del servidor durante el flujo de recuperación de credenciales.
 */
data class ForgotPasswordResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor (ej. credenciales inválidas, etc.)
    val message: String?,

    // Desafío de seguridad. Se declara como Nullable (String?) y con valor por defecto
    // para prevenir excepciones (NullPointerException) si la API omite el campo en caso de error.
    val pregunta: String? = null
)