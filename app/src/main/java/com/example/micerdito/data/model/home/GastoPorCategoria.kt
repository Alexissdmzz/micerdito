package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - GastoPorCategoria
 * Modelo de datos agregado que representa la sumarización
 * financiera por categoría. Está diseñado específicamente para poblar estructuras
 * de visualización de datos como gráficos circulares en el Dashboard.
 */
data class GastoPorCategoria(
    // Etiqueta descriptiva para la leyenda del gráfico.
    // Mapeado explícitamente desde 'nombre' en el JSON.
    @SerializedName("nombre")
    val nombreCategoria: String,

    // Métrica financiera pre-calculada en el servidor
    @SerializedName("totalGasto")
    val totalGasto: Double,

    // Código hexadecimal para la renderización del segmento en la gráfica.
    // Declarado como Nullable (?) para garantizar la estabilidad de la UI si falta el dato.
    @SerializedName("color")
    val color: String? = null
)