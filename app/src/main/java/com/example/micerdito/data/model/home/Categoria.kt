package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("id_categoria") val idCategoria: String,
    val nombre: String,
    val icono: String,
    val color: String
)