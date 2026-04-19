package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - CalendarioResponse
 * Modelo de datos inmutable encargado de deserializar la carga múltiple (Multi-Result Set)
 * devuelta por la API para popular la vista del Calendario y sus gráficos asociados.
 */
data class CalendarioResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación
    val message: String,

    // Fecha original de alta del usuario, utilizada para limitar la navegación histórica del calendario.
    // Se utiliza @SerializedName para adaptar el formato de base de datos al estándar de Kotlin.
    @SerializedName("fecha_registro")
    val fechaRegistro: String?,

    // Colección de días (numéricos) que registran actividad financiera en el mes consultado.
    @SerializedName("dias_con_gastos")
    val diasConGastos: List<Int>,

    // Agregación de métricas financieras desglosadas por categoría para la renderización del gráfico.
    @SerializedName("resumen_grafico")
    val resumenGrafico: List<ResumenCategoria>
)