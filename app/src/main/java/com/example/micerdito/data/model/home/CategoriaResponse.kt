package com.example.micerdito.data.model.home

/**
 * DTO (Data Transfer Object) - CategoriaResponse
 * Modelo de datos inmutable que actúa como envoltorio (Wrapper Response) para deserializar
 * el catálogo completo de categorías devuelto por el servidor.
 * Se utiliza principalmente en el flujo de creación o edición de transacciones.
 */
data class CategoriaResponse(
    // Bandera booleana que determina el flujo de ejecución (Éxito o Fallo de la petición)
    val success: Boolean,

    // Colección de entidades Categoria. Gson mapeará automáticamente el array JSON
    // a esta lista fuertemente tipada mediante reflexión.
    val categorias: List<Categoria>,

    // Mensaje de feedback proporcionado por el servidor para la capa de presentación.
    // Declarado como Nullable (?) por si la API omite el campo en peticiones exitosas para ahorrar ancho de banda.
    val message: String? = null
)