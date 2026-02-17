package com.example.micerdito.data.model.autenticacion

/**
 * Modelo unificado para la recuperación de contraseña.
 * El campo 'pregunta' puede ser nulo porque solo llega en el primer paso.
 */
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String?,
    val pregunta: String? = null
)