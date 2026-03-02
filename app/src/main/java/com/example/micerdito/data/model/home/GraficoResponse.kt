package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - GraficoResponse:
 * Esta clase recoge los datos de la API para listar los gastos por categoría para
 * mostarlos en formato de gráfico en Home.
 */

data class GraficoResponse(
    val success: Boolean,
    @SerializedName("datos") val listaGrafico: List<GastoPorCategoria>
)