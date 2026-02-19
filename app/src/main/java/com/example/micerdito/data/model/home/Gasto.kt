package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName
data class Gasto(
    @SerializedName("id_gasto") val idGasto: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("importe") val importe: Double,
    @SerializedName("fecha_gasto") val fecha: String,
    @SerializedName("icono_categoria") val icono: String,
    @SerializedName("color_categoria") val color: String
)