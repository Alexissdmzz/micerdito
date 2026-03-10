package com.example.micerdito.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto // Asegúrate de que tu modelo tenga estos campos

class GastoAdapter(private var listaGastos: List<Gasto> = emptyList()) :
    RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

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

        // Llenamos los datos basándonos en tu XML
        holder.tvIcono.text = gasto.icono // Ej: "🍔" o "🏠"
        holder.tvTitulo.text = gasto.titulo
        val horaSolo = try {
            gasto.fecha.split(" ")[1].substring(0, 5) // Resultado: "14:30"
        } catch (e: Exception) {
            gasto.fecha // Si falla, ponemos la fecha tal cual
        }
        holder.tvHora.text = horaSolo

        // Formateamos el importe (puedes añadir el símbolo € aquí)
        holder.tvImporte.text = "-${gasto.importe} €"
    }

    override fun getItemCount(): Int = listaGastos.size

    /**
     * Método para actualizar la lista cuando el usuario toque un día diferente
     */
    fun actualizarLista(nuevaLista: List<Gasto>) {
        this.listaGastos = nuevaLista
        notifyDataSetChanged()
    }
}