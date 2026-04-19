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
 * ADAPTADOR - MovimientosAdapter
 * Gestiona la representación visual del historial de transacciones recientes.
 * Actúa como enlace entre la colección de datos financieros y el componente de vista.
 */
class MovimientosAdapter(private val lista: List<Gasto>) :
    RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

    /**
     * PATRÓN VIEW HOLDER
     * Almacena las referencias de los elementos visuales en memoria para optimizar
     * el costo computacional durante el desplazamiento de la interfaz.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionGasto)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaGasto)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoGasto)
    }

    /**
     * Genera la estructura visual base a partir del diseño XML correspondiente.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gastos, parent, false)
        return ViewHolder(view)
    }

    /**
     * Vincula la entidad de dominio con los componentes de la interfaz.
     * Aplica reglas de formato tipográfico y estilos semánticos.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = lista[position]

        // Asignación de atributos base al contenedor visual
        holder.tvIcono.text = gasto.icono
        holder.tvDescripcion.text = gasto.titulo
        holder.tvFecha.text = gasto.fecha

        // Aplicación de máscara de formato monetario
        val montoTexto = "-${String.format("%.2f", gasto.importe)} €"
        holder.tvMonto.text = montoTexto

        // Inyección de color semántico para enfatizar la salida de capital
        holder.tvMonto.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.error_red
            )
        )
    }

    /**
     * Calcula la cardinalidad de la colección de datos actual.
     */
    override fun getItemCount(): Int = lista.size
}