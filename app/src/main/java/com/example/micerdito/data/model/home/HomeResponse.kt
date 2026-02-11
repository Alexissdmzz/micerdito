package com.example.micerdito.data.model.home

/**
 * Data class que actua como un contenedor de datos, en esta clase recogemos la respuesta del servidor al interactuar con la interfaz de home,
 * además de datos necesarios de la BBDD
 */

data class HomeResponse(
    val success: Boolean, //Indica si la operación fue exitosa (True) o no (False)
    val message: String, // Mensaje informativo que viene del servidor
    val nombreUsuario: String, // Nombre del usuario de la BBDD
    val total_dinerogastado: Double, // Total dinero gastado del usuario de la BBDD
    val limite_mes: Double, // Límite puesto por el usuario de la BBDD
    val mes_actual: String // Contiene el mes actual
)

