package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Categoria

/**
 * ADAPTER - CategoriaAdapter:
 * Esta clase es la encargada de gestionar la visualización de las categorías en un RecyclerView.
 * Utiliza el patrón ViewHolder para optimizar el rendimiento al reciclar las vistas de la lista.
 *
 * @param lista Colección de objetos [Categoria] obtenidos desde el repositorio.
 * @param onClick Función de callback (lambda) que se ejecuta al seleccionar una categoría.
 */
class CategoriaAdapter(
    private val lista: List<Categoria>,
    private val onClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    /**
     * ViewHolder: Contenedor de las vistas de cada elemento de la lista.
     * Mantiene las referencias a los componentes XML para evitar llamadas repetitivas a findViewById.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
    }

    /**
     * Infla el diseño XML (item_categoria) para cada fila de la lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    /**
     * Vincula los datos de un objeto Categoria específico con los componentes de la vista.
     * Se encarga de asignar el icono (emoji/texto) y el nombre de la categoría.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = lista[position]

        // Asignación de datos al componente visual
        holder.tvIcono.text = cat.icono
        holder.tvNombre.text = cat.nombre

        // Configuración del evento de clic mediante la lambda recibida en el constructor
        holder.itemView.setOnClickListener { onClick(cat) }
    }

    /**
     * Retorna el número total de elementos a mostrar.
     */
    override fun getItemCount() = lista.size
}