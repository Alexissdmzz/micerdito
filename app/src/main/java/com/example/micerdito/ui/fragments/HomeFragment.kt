package com.example.micerdito.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.micerdito.R
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.viewmodel.home.HomeViewModel
import com.github.mikephil.charting.charts.PieChart

/**
 * @HomeFragment es la clase donde definimos los elementos interactivos del xml @fragment_home para el usuario
 * en la pantalla de Home.
 */

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels() // Herramienta que nos deja conectar con la Lógica (ViewModel)
    private lateinit var graficoCircular: PieChart // Herramiento para el gráfico de gastos

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // INICIALIZAMOS ELEMENTOS INTERACTIVOS
        val tvGasto = view.findViewById<TextView>(R.id.tvTotalSpent)
        val tvLimite = view.findViewById<TextView>(R.id.tvLimitStatus)
        val tvMes = view.findViewById<TextView>(R.id.tvMesActual)
        graficoCircular = view.findViewById(R.id.graficoCircular)

        setupObservers(tvGasto, tvLimite, tvMes) // Observador

        // CARGAMOS LOS DATOS DEL USUARIO GUARDADOS EN LAS PREFERENCIAS
        val preferenciasSesion = PreferenciasSesion(requireContext())
        viewModel.cargarDatosDeUsuario(preferenciasSesion)
    }

    private fun setupObservers(tvGasto: TextView, tvLimite: TextView, tvMes: TextView) {
        // MOSTRAMOS LOS DATOS
        viewModel.homeResult.observe(viewLifecycleOwner) { data ->
            tvGasto.text = "${data.total_dinerogastado} €"
            tvLimite.text = "Límite: ${data.limite_mes} €"
            tvMes.text = data.mes_actual

        }
        // SI EL DINERO GASTADO SUPERA EL LÍMITE SE PONDRA DE COLOR ROJO
        viewModel.islimiteSuperado.observe(viewLifecycleOwner) { superado ->
            tvGasto.setTextColor(if (superado) Color.RED else Color.parseColor("#4CAF50"))
        }
    }

}