package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - ResumenCategoria
 * Modelo de datos agregado utilizado para deserializar
 * las métricas financieras específicas de la vista del Calendario.
 */
data class ResumenCategoria(
    // Etiqueta descriptiva de la agrupación para la leyenda de la interfaz
    @SerializedName("nombre")
    val nombre: String,

    // Código hexadecimal para la renderización de los elementos gráficos.
    // Declarado como Nullable (?) como medida de seguridad (Failsafe) para la UI.
    @SerializedName("color")
    val color: String?,

    // Sumatorio total de la categoría procesado y devuelto por el motor de base de datos
    @SerializedName("total")
    val total: Double
)