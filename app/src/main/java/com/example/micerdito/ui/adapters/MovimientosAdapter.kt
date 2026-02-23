package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto

/**
 * ADAPTER - MovimientosAdapter
 * Responsable de la representación visual de la lista de gastos (movimientos) en el Dashboard.
 * Transforma los objetos de datos [Gasto] en elementos de lista legibles, aplicando
 * formatos de moneda y estilos visuales de alerta.
 */
class MovimientosAdapter(private val lista: List<Gasto>) :
    RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

    /**
     * ViewHolder: Enlaza los componentes del layout XML 'item_gastos' con el código Kotlin.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionGasto)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaGasto)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoGasto)
    }

    /**
     * Infla la vista de cada fila de gasto.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gastos, parent, false)
        return ViewHolder(view)
    }

    /**
     * Procesa y muestra los datos de cada gasto individual.
     * Incluye lógica de formateo para mejorar la experiencia de usuario (UX).
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = lista[position]

        // Mostramos el icono visual de la categoría (Emoji/Icono) y textos descriptivos
        holder.tvIcono.text = gasto.icono
        holder.tvDescripcion.text = gasto.titulo
        holder.tvFecha.text = gasto.fecha

        // Formateamos el importe
        val montoTexto = "-${String.format("%.2f", gasto.importe)}€"
        holder.tvMonto.text = montoTexto

        /**
         * ESTILO VISUAL:
         * Se aplica un color semántico (rojo) para identificar rápidamente los gastos.
         */
        holder.tvMonto.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.error_red
            )
        )
    }

    /**
     * Indica el tamaño de la lista de movimientos recientes.
     */
    override fun getItemCount(): Int = lista.size
}