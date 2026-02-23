package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - Categoria:
 * Esta clase recoge los datos de la API para añadir a la lista de Categorias
 * y enseñarla en el fragmento Añadir Gasto.
 */
data class Categoria(
    @SerializedName("id_categoria") val idCategoria: String, // Recoge el id de la categoria
    val nombre: String, // Recoge el nombre de la categoria
    val icono: String, // Recoge el icono de la categoria
    val color: String // Recoge el color de la categoria
)