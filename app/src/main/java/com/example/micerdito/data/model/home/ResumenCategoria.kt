package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - CalendarioResponse:
 * Esta clase recoge los datos de la API para el fragmento de Calendario.
 */
data class ResumenCalendario(
    val nombre: String,  // Nombre de la categoría
    val color: String,   // Color de la categoría
    val total: Double    // Importe del gasto
)