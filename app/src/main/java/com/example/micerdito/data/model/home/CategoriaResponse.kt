package com.example.micerdito.data.model.home

data class CategoriaResponse(
    val success: Boolean,
    val categorias: List<Categoria>,
    val message: String? = null
)