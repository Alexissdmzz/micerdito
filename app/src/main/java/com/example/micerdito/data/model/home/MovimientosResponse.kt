package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

data class MovimientosResponse (
    val success: Boolean,
    @SerializedName("gastos_recientes") val gastosRecientes: List<Gasto>
)