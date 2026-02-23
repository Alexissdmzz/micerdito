package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - MovimientosResponse:
 * Esta clase recoge los datos de la API de los movimientos para el fragmento de Home.
 */
data class MovimientosResponse (
    val success: Boolean, // Indica si la operación fue exitosa (True) o no (False)
    @SerializedName("gastos_recientes") val gastosRecientes: List<Gasto> // Lista de los gastos recientes
)