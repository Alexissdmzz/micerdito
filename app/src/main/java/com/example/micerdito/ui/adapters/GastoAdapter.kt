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
 * Controlador encargado de la representación visual de los gastos registrados en la vista
 * de calendario. Gestiona la transformación de datos del modelo [Gasto] a componentes de la UI.
 */
class GastoAdapter(private var listaGastos: List<Gasto> = emptyList()) :
    RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    // Listener para la comunicación de eventos de selección hacia la vista superior
    var onItemClick: ((Gasto) -> Unit)? = null

    /**
     * VIEW HOLDER:
     * Cache de referencias visuales para optimizar el scroll y evitar inflados innecesarios.
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

    /**
     * VINCULACIÓN Y FORMATEO:
     * Transfiere la información del objeto Gasto a los widgets correspondientes,
     * realizando transformaciones de cadena para la hora y el símbolo monetario.
     */
    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = listaGastos[position]

        holder.tvIcono.text = gasto.icono
        holder.tvTitulo.text = gasto.titulo

        // Lógica de extracción de segmento horario (Format: "HH:mm")
        val horaSolo = try {
            gasto.fecha.split(" ")[1].substring(0, 5)
        } catch (e: Exception) {
            gasto.fecha
        }
        holder.tvHora.text = horaSolo

        // Inyección de máscara monetaria
        holder.tvImporte.text = "-${gasto.importe} €"

        // Disparo de evento de clic a través de la función de orden superior
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(gasto)
        }
    }

    override fun getItemCount(): Int = listaGastos.size

    /**
     * REFRESCO DINÁMICO:
     * Actualiza la referencia de la lista en memoria y notifica al observador
     * para redibujar los elementos en pantalla.
     */
    fun actualizarLista(nuevaLista: List<Gasto>) {
        this.listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}