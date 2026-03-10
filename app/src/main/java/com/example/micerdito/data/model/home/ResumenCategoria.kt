package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - ResumenCategoria:
 * Esta clase recoge los datos de la API para mla vista del fragmento Calendario.
 */
data class ResumenCategoria(
    val nombre: String,  // Nombre de la categoría
    val color: String,   // Color de la categoría
    val total: Double    // Importe del gasto
)