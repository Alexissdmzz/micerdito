package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - Gasto:
 * Esta clase recoge los datos de la API para añadir el gasto y enseñarla en el fragmento Home o Calendario.
 */
data class Gasto(
    @SerializedName("id_gasto") val idGasto: String, // Recoge el id del gasto añadido
    @SerializedName("titulo") val titulo: String, // Recoge el titulo del gasto añadido
    @SerializedName("importe") val importe: Double, // Recoge el importe del gasto añadido
    @SerializedName("fecha_gasto") val fecha: String, // Recoge la fecha de gasto del gasto añadido
    @SerializedName("icono_categoria") val icono: String, // Recoge el icono de la categoria de la categoria seleccionada
    @SerializedName("color_categoria") val color: String // Recoge el color de la categoria de la categoria seleccionada
)