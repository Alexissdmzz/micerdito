package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - GraficoResponse
 * Modelo de datos inmutable que actúa como envoltorio (Wrapper Response) para deserializar
 * la agregación financiera. Está diseñado específicamente para alimentar el motor
 * de visualización de datos (Gráfico Circular) en la pantalla principal (Dashboard).
 */
data class GraficoResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Colección de métricas agregadas.
    // Se utiliza un mapeo semántico (@SerializedName) para transformar la clave genérica
    // del servidor ("datos") en un identificador con contexto de negocio ("listaGrafico").
    @SerializedName("datos")
    val listaGrafico: List<GastoPorCategoria>
)