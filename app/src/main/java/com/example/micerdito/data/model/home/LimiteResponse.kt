package com.example.micerdito.data.model.home

/**
 * DTO (Data Transfer Object) - LimiteResponse
 * Modelo de datos inmutable utilizado para deserializar la confirmación del servidor
 * tras la operación de mutación (actualización) del umbral o presupuesto mensual del usuario.
 */
data class LimiteResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la operación)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación
    val message: String
)