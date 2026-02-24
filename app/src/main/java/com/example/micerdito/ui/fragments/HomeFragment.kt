package com.example.micerdito.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.ui.adapters.MovimientosAdapter
import com.example.micerdito.viewmodel.home.HomeViewModel
import com.github.mikephil.charting.charts.PieChart

/**
 * FRAGMENTO - HomeFragment:
 * Actúa como el centro de control (Dashboard) del usuario.
 * Muestra el resumen financiero del mes actual, el estado del presupuesto
 * y el historial de transacciones recientes mediante una arquitectura reactiva.
 */

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Inicialización del viewmodel
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización de componentes de la vista
        val tvGasto = view.findViewById<TextView>(R.id.tvTotalSpent)
        val tvLimite = view.findViewById<TextView>(R.id.tvLimitStatus)
        val tvMes = view.findViewById<TextView>(R.id.tvMesActual)
        val tvEstablecerLimite = view.findViewById<TextView>(R.id.tvEstablecerLimite)
        val graficoCircular = view.findViewById<PieChart>(R.id.graficoCircular)
        val rvGastos = view.findViewById<RecyclerView>(R.id.rvGastos)

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers(tvGasto, tvLimite, tvMes, rvGastos)

        // Configuración de interraciones
        setupListeners(tvEstablecerLimite)
    }

    /**
     * Observadores de estado: Reaccionan a los cambios en el flujo de datos.
     */
    private fun setupObservers(tvGasto: TextView, tvLimite: TextView, tvMes: TextView, rvGastos: RecyclerView) {

        // OBSERVADOR 1: Datos globales del mes (Total gastado y Límite)
        viewModel.homeResult.observe(viewLifecycleOwner) { data ->
            tvGasto.text = "${data.total_dinerogastado} €"
            tvLimite.text = "Límite: ${data.limite_mes} €"
            tvMes.text = data.mes_actual
        }

        // OBSERVADOR 2: Lista de movimientos recientes
        viewModel.movimientosResult.observe(viewLifecycleOwner) { lista ->
            android.util.Log.d("DEBUG_MOV", "Han llegado ${lista.size} gastos")
            if (!lista.isNullOrEmpty()) {
                // Se asigna el adaptador con la lista de gastos procesada
                rvGastos.adapter = MovimientosAdapter(lista)
            }
        }

        /**
         * LÓGICA SEMÁNTICA DE COLOR:
         * Cambia el color del texto del gasto según el estado del presupuesto.
         * Rojo (Superado) / Verde (Dentro del límite).
         */
        viewModel.islimiteSuperado.observe(viewLifecycleOwner) { superado ->
            tvGasto.setTextColor(if (superado) Color.RED else Color.parseColor("#4CAF50"))
        }
    }

    /**
     * Configuración de interacciones del usuario.
     */
    private fun setupListeners(tvEstablecerLimite: TextView) {
        /**
         * Establecer Límite: muestra un diálogo de entrada de datos para actualizar el presupuesto mensual.
         * Utiliza un AlertDialog con un componente EditText configurado para valores numéricos.
         */
        tvEstablecerLimite.setOnClickListener {
            val input = EditText(requireContext())
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Límite Mensual")
                .setMessage("¿Cuánto quieres gastar este mes?")
                .setView(input)
                .setPositiveButton("Guardar") { _, _ ->
                    val nuevoLimite = input.text.toString()
                    if (nuevoLimite.isNotEmpty()) {
                        // Notifica al ViewModel el cambio para su persistencia en el servidor PHP
                        viewModel.actualizarLimiteMensual(nuevoLimite.toDouble())
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    }

    /**
     * Ciclo de vida: Carga o refresca los datos cada vez que el fragmento vuelve a ser visible.
     * Esto asegura que si se añadió un gasto en otro fragmento, el Dashboard esté actualizado.
     */
    override fun onResume() {
        super.onResume()
        viewModel.cargarDatosDeUsuario()
    }
}