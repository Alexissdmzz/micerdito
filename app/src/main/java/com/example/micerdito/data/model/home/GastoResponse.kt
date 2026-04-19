package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - GastoResponse
 * Modelo de datos inmutable que actúa como envoltorio (Wrapper Response) multipropósito
 * para las operaciones CRUD (Crear, Leer, Actualizar, Borrar) de la entidad Gasto.
 */
data class GastoResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación
    val message: String,

    // Identificador del gasto afectado en operaciones de mutación (Insert, Update, Delete).
    // Se aplica camelCase en Kotlin mapeándolo contra el snake_case del backend.
    @SerializedName("id_gasto")
    val idGasto: String?,

    // Colección de gastos utilizada exclusivamente en operaciones de lectura.
    // Declarada como Nullable (?) para ahorrar memoria cuando solo se confirma una mutación.
    @SerializedName("data")
    val data: List<Gasto>?
)