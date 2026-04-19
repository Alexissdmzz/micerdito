package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - AjustesResponse
 * Modelo de datos inmutable utilizado para deserializar la respuesta de las operaciones
 * de configuración y gestión de cuenta (edición de perfil, borrado, etc.).
 */
data class AjustesResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la operación)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación
    val message: String,

    // Identificador único de la entidad afectada. Se utiliza @SerializedName para aislar
    // la convención de nombres del backend (snake_case) del estándar de Kotlin (camelCase).
    @SerializedName("id_usuario")
    val idUsuario: String
)