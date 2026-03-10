package com.example.micerdito.data.model.home

/**
 * MODELO DE DATOS - CalendarioResponse:
 * Esta clase recoge los datos de la API para el fragmento de Calendario.
 */
data class CalendarioResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val fecha_registro: String?,      // Variable donde guardamos la fecha de registro del usuario
    val dias_con_gastos: List<Int>,   // Lista que guarda aquellos días que contengan gastos
    val resumen_grafico: List<ResumenCategoria> // Lista que guarda los gastos para el gráfico
)