package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto

/**
 * ADAPTADOR - GastoAdapter:
 * Gestiona la visualización en formato lista de los gastos diarios dentro del calendario.
 * Vincula los datos del modelo Gasto con la vista item_gasto_calendario.xml.
 */
class GastoAdapter(private var listaGastos: List<Gasto> = emptyList()) :
    RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    // Función de orden superior para delegar el evento de clic al componente padre (Fragment/Activity)
    var onItemClick: ((Gasto) -> Unit)? = null

    /**
     * VIEWHOLDER - GastoViewHolder:
     * Mantiene las referencias a los elementos de la interfaz de usuario para cada ítem de la lista.
     */
    class GastoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCat)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloGasto)
        val tvHora: TextView = view.findViewById(R.id.tvHoraGasto)
        val tvImporte: TextView = view.findViewById(R.id.tvImporteGasto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return GastoViewHolder(
            layoutInflater.inflate(
                R.layout.item_gasto_calendario,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = listaGastos[position]

        // Asignación de datos a los componentes visuales
        holder.tvIcono.text = gasto.icono
        holder.tvTitulo.text = gasto.titulo

        // Extracción y formateo de la hora a partir de la fecha de registro (Formato esperado: "YYYY-MM-DD HH:MM:SS")
        val horaSolo = try {
            gasto.fecha.split(" ")[1].substring(0, 5)
        } catch (e: Exception) {
            gasto.fecha
        }
        holder.tvHora.text = horaSolo

        // Formateo del importe monetario
        holder.tvImporte.text = "-${gasto.importe} €"

        // Configuración del evento de selección del ítem completo
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(gasto)
        }
    }

    override fun getItemCount(): Int = listaGastos.size

    /**
     * Actualiza el conjunto de datos del adaptador y refresca la interfaz de usuario.
     * @param nuevaLista Colección actualizada de objetos Gasto a mostrar.
     */
    fun actualizarLista(nuevaLista: List<Gasto>) {
        this.listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}