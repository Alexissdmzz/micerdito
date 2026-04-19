package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Categoria

/**
 * ADAPTADOR - CategoriaAdapter
 * Gestiona la cuadrícula de categorías en la pantalla de añadir gastos.
 * Se encarga de dos tareas clave:
 * 1. Dibujar cada categoría usando los datos de la lista.
 * 2. Avisar a la pantalla principal cuando el usuario toca una de ellas.
 *
 * @param lista Colección de entidades Categoria a renderizar.
 * @param onClick Interfaz funcional para delegar el evento de selección a la capa superior.
 */
class CategoriaAdapter(
    private val lista: List<Categoria>,
    private val onClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    /**
     * PATRÓN VIEW HOLDER
     * Guarda las referencias a los textos y elementos visuales de cada categoría.
     * Esto evita que la app tenga que buscarlos desde cero cada vez que hacemos scroll,
     * mejorando mucho el rendimiento.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
    }

    /**
     * Crea la vista en blanco para un nuevo elemento de la lista leyendo el archivo XML.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    /**
     * Rellena la vista en blanco con los datos reales de la categoría (icono y nombre).
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = lista[position]

        holder.tvIcono.text = cat.icono
        holder.tvNombre.text = cat.nombre

        // Configura el clic para avisar al fragmento de qué categoría se acaba de pulsar
        holder.itemView.setOnClickListener { onClick(cat) }
    }

    /**
     * Devuelve el número total de categorías que hay para mostrar.
     */
    override fun getItemCount() = lista.size
}