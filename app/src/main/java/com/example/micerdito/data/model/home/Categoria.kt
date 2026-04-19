package com.example.micerdito.data.model.home

import com.google.gson.annotations.SerializedName

/**
 * DTO (Data Transfer Object) - Entidad Categoria
 * Modelo de dominio inmutable que representa una categoría de transacción financiera.
 * Se utiliza principalmente para poblar selectores (Spinners o RecyclerViews) en la capa de presentación.
 */
data class Categoria(
    // Identificador único de la categoría. Se emplea @SerializedName para mantener
    // el estándar camelCase de Kotlin aislando el snake_case del backend.
    @SerializedName("id_categoria")
    val idCategoria: String,

    // Etiqueta de texto descriptiva para la visualización en la interfaz de usuario
    val nombre: String,

    // Referencia al asset gráfico para ilustrar la categoría
    val icono: String,

    // Código de color para la tematización de elementos visuales en la UI
    val color: String
)