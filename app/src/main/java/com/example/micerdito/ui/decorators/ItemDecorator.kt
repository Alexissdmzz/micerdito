package com.example.micerdito.ui.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * DECORADOR - ItemDecorator:
 * Añade espacio entre los elementos de un RecyclerView con GridLayout.
 *
 * Se usa principalmente para separar visualmente tarjetas (como categorías)
 * y evitar que queden pegadas entre sí.
 *
 * @param spanCount Número de columnas del grid (Ej: 5 categorías por fila).
 * @param spacing Espacio entre elementos en píxeles (usar dimens.xml).
 * @param includeEdge Indica si también se aplica espacio en los bordes exteriores.
 */
class ItemDecorator(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    /**
     * Este método se ejecuta para cada item del RecyclerView.
     * Aquí definimos los márgenes (offsets) alrededor de cada elemento.
     */
    override fun getItemOffsets(
        outRect: Rect,      // Rectángulo donde se aplican los márgenes del item
        view: View,         // Vista del item actual
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // posición del item
        val column = position % spanCount // columna actual dentro del grid

        if (includeEdge) {
            // 👉 Incluye espacio en los bordes exteriores del RecyclerView

            // Margen izquierdo del item
            outRect.left = spacing - column * spacing / spanCount

            // Margen derecho del item
            outRect.right = (column + 1) * spacing / spanCount

            // Solo la primera fila tiene margen superior
            if (position < spanCount) {
                outRect.top = spacing
            }

            // Todos los items tienen margen inferior
            outRect.bottom = spacing

        } else {
            // No incluye espacio en los bordes exteriores

            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            // Solo las filas que no son la primera tienen margen superior
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}