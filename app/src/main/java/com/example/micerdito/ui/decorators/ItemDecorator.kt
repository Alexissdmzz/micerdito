package com.example.micerdito.ui.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * DECORADOR - ItemDecorator
 * Componente visual encargado de inyectar márgenes y separaciones uniformes
 * entre los elementos de una lista estructurada en formato de cuadrícula.
 *
 * Actúa sobre el proceso de dibujado del RecyclerView para garantizar que las tarjetas
 * mantengan una distancia consistente, mejorando la legibilidad de la interfaz.
 *
 * @param spanCount Número de columnas que componen la cuadrícula.
 * @param spacing Dimensión de la separación entre elementos expresada en píxeles.
 * @param includeEdge Determina si los márgenes también se aplican a los límites exteriores de la lista.
 */
class ItemDecorator(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    /**
     * Intercepta el cálculo de las dimensiones de cada elemento antes de ser renderizado.
     * Aplica la distribución matemática para asegurar que todos los elementos y columnas
     * tengan exactamente el mismo ancho y separación.
     */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // Posición del elemento
        val column = position % spanCount // Columna actual dentro de la cuadrícula

        if (includeEdge) {
            // Configuración con márgenes en los límites exteriores de la cuadrícula
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            // Aplica margen superior únicamente a los elementos de la primera fila
            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing

        } else {
            // Configuración restringida exclusivamente al espacio interno entre elementos
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            // Aplica margen superior a todas las filas exceptuando la primera
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}