package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Categoria

class CategoriaAdapter(
    private val lista: List<Categoria>,
    private val onClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = lista[position]
        holder.tvIcono.text = cat.icono
        holder.tvNombre.text = cat.nombre
        holder.itemView.setOnClickListener { onClick(cat) }
    }

    override fun getItemCount() = lista.size
}