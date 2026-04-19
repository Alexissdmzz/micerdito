package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - HomeResponse
 * Modelo de datos inmutable que actúa como envoltorio de agregación.
 * Deserializa toda la información necesaria para renderizar el Dashboard en una única
 * petición de red, optimizando los recursos del dispositivo.
 */
data class HomeResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación
    val message: String,

    // Nombre de visualización del usuario para la cabecera (Header) de la UI.
    // Se mapea para proteger la convención camelCase de Kotlin.
    @SerializedName("nombre_usuario")
    val nombreUsuario: String,

    // Métrica agregada con el volumen total de gasto en el periodo actual.
    // Adaptación de snake_case a camelCase.
    @SerializedName("total_dinerogastado")
    val totalDineroGastado: Double,

    // Umbral financiero (presupuesto) configurado por el usuario para el mes en curso.
    @SerializedName("limite_mes")
    val limiteMes: Double,

    // Etiqueta temporal (ej. "Enero", "Febrero") para contextualizar la vista principal.
    @SerializedName("mes_actual")
    val mesActual: String,

    // Colección de las últimas transacciones para poblar el RecyclerView principal.
    // Reutiliza la entidad de dominio 'Gasto', manteniendo el principio DRY.
    @SerializedName("gastos_recientes")
    val gastosRecientes: List<Gasto>
)