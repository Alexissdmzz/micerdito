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
 * ADAPTADOR - MovimientosAdapter:
 * Gestiona la representación de la lista de gastos recientes en el Dashboard principal.
 * Se encarga de transformar los objetos [Gasto] en componentes visuales dinámicos,
 * aplicando reglas de estilo y formato monetario.
 */
class MovimientosAdapter(private val lista: List<Gasto>) :
    RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

    /**
     * VIEW HOLDER:
     * Almacena las referencias a los elementos del layout 'item_gastos' para optimizar
     * el rendimiento del scroll mediante la reutilización de memoria.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionGasto)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaGasto)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoGasto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gastos, parent, false)
        return ViewHolder(view)
    }

    /**
     * LÓGICA DE REPRESENTACIÓN:
     * Inyecta los datos del modelo en la UI y aplica reglas de negocio visuales,
     * como el formateo de decimales y la asignación de colores por tipo de movimiento.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = lista[position]

        // Enlace de datos descriptivos
        holder.tvIcono.text = gasto.icono
        holder.tvDescripcion.text = gasto.titulo
        holder.tvFecha.text = gasto.fecha

        // Formateo de precisión monetaria a dos decimales y adición de símbolo local
        val montoTexto = "-${String.format("%.2f", gasto.importe)}€"
        holder.tvMonto.text = montoTexto

        /**
         * FEEDBACK VISUAL SEMÁNTICO:
         * Aplicación de un color identificativo para enfatizar que el registro
         * corresponde a una salida de capital (Gasto).
         */
        holder.tvMonto.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.error_red
            )
        )
    }

    /**
     * CARDINALIDAD:
     * Define el volumen de registros a renderizar en el flujo del Dashboard.
     */
    override fun getItemCount(): Int = lista.size
}