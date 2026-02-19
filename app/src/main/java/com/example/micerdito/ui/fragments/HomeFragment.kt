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

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var graficoCircular: PieChart
    private lateinit var rvGastos: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // INICIALIZAMOS ELEMENTOS INTERACTIVOS
        val tvGasto = view.findViewById<TextView>(R.id.tvTotalSpent)
        val tvLimite = view.findViewById<TextView>(R.id.tvLimitStatus)
        val tvMes = view.findViewById<TextView>(R.id.tvMesActual)
        val tvEstablecerLimite = view.findViewById<TextView>(R.id.tvEstablecerLimite)
        graficoCircular = view.findViewById(R.id.graficoCircular)
        rvGastos = view.findViewById(R.id.rvGastos)

        setupObservers(tvGasto, tvLimite, tvMes)

        tvEstablecerLimite.setOnClickListener { mostrarConfirmacionLimite() }
    }

    private fun setupObservers(tvGasto: TextView, tvLimite: TextView, tvMes: TextView) {
        // OBSERVADOR 1: Totales y Resumen
        viewModel.homeResult.observe(viewLifecycleOwner) { data ->
            tvGasto.text = "${data.total_dinerogastado} €"
            tvLimite.text = "Límite: ${data.limite_mes} €"
            tvMes.text = data.mes_actual
        }

        // OBSERVADOR 2: Movimientos Recientes (La nueva lista separada)
        viewModel.movimientosResult.observe(viewLifecycleOwner) { lista ->
            android.util.Log.d("DEBUG_MOV", "Han llegado ${lista.size} gastos")
            if (!lista.isNullOrEmpty()) {
                rvGastos.adapter = MovimientosAdapter(lista)
            }
        }

        // CONTROL DEL COLOR DEL GASTO
        viewModel.islimiteSuperado.observe(viewLifecycleOwner) { superado ->
            tvGasto.setTextColor(if (superado) Color.RED else Color.parseColor("#4CAF50"))
        }
    }

    private fun mostrarConfirmacionLimite() {
        val input = EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Límite Mensual")
            .setMessage("¿Cuánto quieres gastar este mes?")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoLimite = input.text.toString()
                if (nuevoLimite.isNotEmpty()) {
                    viewModel.actualizarLimiteMensual(nuevoLimite.toDouble())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarDatosDeUsuario()
    }
}