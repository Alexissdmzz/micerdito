package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto

class MovimientosAdapter(private val lista: List<Gasto>) :
    RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = lista[position]

        holder.tvIcono.text = gasto.icono
        holder.tvDescripcion.text = gasto.titulo
        holder.tvFecha.text = gasto.fecha

        // Formateamos el importe (Ej: -25.50€)
        val montoTexto = "-${String.format("%.2f", gasto.importe)}€"
        holder.tvMonto.text = montoTexto

        // Opcional: Si quieres que el color dependa de la categoría o el importe
        holder.tvMonto.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error_red))
    }

    override fun getItemCount(): Int = lista.size
}