package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - GastoPorCategoria:
 * Esta clase recoge los datos de la API para listar los gastos por categoría para
 * mostarlos en formato de gráfico en Home.
 */

data class GastoPorCategoria (
    @SerializedName("nombre") val nombreCategoria: String, // Nombre de la categoría
    @SerializedName("totalGasto") val totalGasto: Double, // Suma total de los gastos de esa categoría
    @SerializedName("color") val color: String? = null // Color de la categoría
)