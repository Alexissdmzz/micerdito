package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - Entidad Gasto
 * Modelo de dominio principal que representa una transacción financiera individual.
 * Se utiliza para mapear la respuesta JSON del servidor y renderizar las listas
 * (RecyclerViews) en el Dashboard y el Calendario.
 */
data class Gasto(
    // Identificador único (UUID) del registro. Se aplica camelCase en Kotlin
    // mapeándolo contra el snake_case del backend.
    @SerializedName("id_gasto")
    val idGasto: String,

    // Concepto descriptivo o título asignado al movimiento
    @SerializedName("titulo")
    val titulo: String,

    // Magnitud financiera de la transacción
    @SerializedName("importe")
    val importe: Double,

    // Marca temporal (Timestamp) del registro (Formato ISO / YYYY-MM-DD)
    @SerializedName("fecha_gasto")
    val fecha: String,

    // Notas adicionales. Declarado como Nullable (?) para prevenir NullPointerExceptions
    // en gastos sin información extendida.
    @SerializedName("descripcion")
    val descripcion: String?,

    // Metadato visual para la UI inyectado desde la relación con la tabla Categorías
    @SerializedName("icono_categoria")
    val icono: String,

    // Código hexadecimal de color para la tematización del ítem en la lista
    @SerializedName("color_categoria")
    val color: String,

    // Referencia física o URL del comprobante multimedia. Nullable (?) si no hay adjunto.
    @SerializedName("foto_ticket")
    val fotoTicket: String?
)