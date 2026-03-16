package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Categoria

/**
 * ADAPTADOR - CategoriaAdapter:
 * Actúa como puente entre la fuente de datos de categorías y el componente RecyclerView.
 * Implementa el patrón ViewHolder para la reutilización eficiente de vistas.
 *
 * @param lista Colección de objetos [Categoria] a renderizar.
 * @param onClick Interfaz funcional (lambda) para gestionar la selección de una categoría.
 */
class CategoriaAdapter(
    private val lista: List<Categoria>,
    private val onClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    /**
     * PATRÓN VIEW HOLDER:
     * Almacena las referencias de los componentes visuales de un ítem para evitar
     * el costo computacional de búsquedas repetitivas (findViewById).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCategoria)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCategoria)
    }

    /**
     * INFLADO DE VISTA:
     * Crea y devuelve una instancia de ViewHolder vinculada al layout XML individual.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return ViewHolder(view)
    }

    /**
     * VINCULACIÓN DE DATOS (BINDING):
     * Mapea las propiedades del objeto [Categoria] a los elementos visuales del ViewHolder.
     * Configura el listener de interacción para la navegación o selección.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = lista[position]

        // Asignación de contenido multimedia (Emoji/Icono) y texto
        holder.tvIcono.text = cat.icono
        holder.tvNombre.text = cat.nombre

        // Propagación del evento de clic hacia el controlador superior
        holder.itemView.setOnClickListener { onClick(cat) }
    }

    /**
     * CONTROL DE VOLUMEN:
     * Retorna la cardinalidad de la lista de categorías.
     */
    override fun getItemCount() = lista.size
}