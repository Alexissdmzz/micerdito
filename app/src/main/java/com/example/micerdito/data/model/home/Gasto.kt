package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * MODELO DE DATOS - Gasto:
 * Representa la entidad detallada de un gasto. Esta clase se utiliza para mapear la
 * respuesta JSON del servidor y facilitar la gestión de datos en el Calendario y Home.
 */
data class Gasto(
    @SerializedName("id_gasto")
    val id_gasto: String, // Identificador único (UUID) del registro en la base de datos

    @SerializedName("titulo")
    val titulo: String, // Concepto o nombre breve asignado al gasto

    @SerializedName("importe")
    val importe: Double, // Valor numérico del gasto realizado

    @SerializedName("fecha_gasto")
    val fecha: String, // Fecha y hora del registro (Formato: YYYY-MM-DD HH:MM:SS)

    @SerializedName("descripcion")
    val descripcion: String?, // Nota adicional o detalle opcional del movimiento

    @SerializedName("icono_categoria")
    val icono: String, // Representación visual (Emoji o ID) de la categoría

    @SerializedName("color_categoria")
    val color: String, // Código hexadecimal del color asociado a la categoría

    @SerializedName("foto_ticket")
    val foto_ticket: String? // Nombre del archivo o ruta de la imagen del comprobante
)