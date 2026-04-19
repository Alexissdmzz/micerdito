package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - MovimientosResponse
 * Modelo de datos inmutable que actúa como envoltorio (Wrapper Response) para deserializar
 * el historial de transacciones. Diseñado específicamente para alimentar listas en pantalla
 * (RecyclerViews) orientadas a mostrar los últimos movimientos o resultados de paginación.
 */
data class MovimientosResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Colección de las transacciones financieras recientes.
    // Se aplica un mapeo explícito para proteger el estándar camelCase de Kotlin.
    @SerializedName("gastos_recientes")
    val gastosRecientes: List<Gasto>
)