package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto

/**
 * ADAPTADOR - GastoAdapter
 * Se encarga de mostrar la lista del historial de gastos (por ejemplo, al pulsar un día en el calendario).
 * Recibe una lista de gastos y los va dibujando uno a uno en la pantalla.
 */
class GastoAdapter(private var listaGastos: List<Gasto> = emptyList()) :
    RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    // Interfaz funcional para avisar a la pantalla principal cuando se toca un gasto
    var onItemClick: ((Gasto) -> Unit)? = null

    /**
     * PATRÓN VIEW HOLDER
     * Guarda las referencias a los textos e iconos de cada gasto.
     * Así no tenemos que buscarlos desde cero cada vez que el usuario hace scroll, y la lista va fluida.
     */
    class GastoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tvIconoCat)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloGasto)
        val tvHora: TextView = view.findViewById(R.id.tvHoraGasto)
        val tvImporte: TextView = view.findViewById(R.id.tvImporteGasto)
    }

    /**
     * Crea la "plantilla" en blanco para un nuevo gasto leyendo el archivo XML.
     */
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

    /**
     * Coge la plantilla en blanco y la rellena con los datos reales del gasto que toca en esa posición.
     * También recorta la fecha para sacar solo la hora y le da formato al dinero.
     */
    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = listaGastos[position]

        holder.tvIcono.text = gasto.icono
        holder.tvTitulo.text = gasto.titulo

        // Recorte de seguridad para sacar solo la hora (ej: "14:30") de la fecha completa
        val horaSolo = try {
            gasto.fecha.split(" ")[1].substring(0, 5)
        } catch (e: Exception) {
            android.util.Log.e("GastoAdapter", "Error formateando fecha: ${gasto.fecha}", e)
            gasto.fecha
        }
        holder.tvHora.text = horaSolo

        // Le ponemos el formato correcto de dos decimales al dinero
        holder.tvImporte.text = "-${String.format("%.2f", gasto.importe)} €"

        // Configura el clic para avisar de qué gasto exacto se acaba de pulsar
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(gasto)
        }
    }

    /**
     * Devuelve la cantidad total de gastos que hay actualmente en la lista.
     */
    override fun getItemCount(): Int = listaGastos.size

    /**
     * Cambia la lista actual por una nueva y avisa a la pantalla para que se vuelva a dibujar entera.
     */
    fun actualizarLista(nuevaLista: List<Gasto>) {
        this.listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}