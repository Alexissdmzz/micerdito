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
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.ui.adapters.MovimientosAdapter
import com.example.micerdito.viewmodel.home.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

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
        setupObservers(tvGasto, tvLimite, tvMes, rvGastos, graficoCircular)

        // Configuración de interraciones
        setupListeners(tvEstablecerLimite)
    }

    /**
     * Observadores de estado: Reaccionan a los cambios en el flujo de datos.
     */
    private fun setupObservers(
        tvGasto: TextView,
        tvLimite: TextView,
        tvMes: TextView,
        rvGastos: RecyclerView,
        graficoCircular: PieChart
    ) {

        // OBSERVADOR 1: Datos globales del mes (Total gastado y Límite)
        viewModel.homeResult.observe(viewLifecycleOwner) { data ->
            tvGasto.text = "${data.total_dinerogastado} €"
            tvLimite.text = "Límite: ${data.limite_mes} €"
            tvMes.text = data.mes_actual.uppercase()
        }

        // OBSERVADOR 2: Datos del Gráfico Circular
        viewModel.graficoResult.observe(viewLifecycleOwner) { lista ->
            if (!lista.isNullOrEmpty()) {
                actualizarGrafico(lista)
            } else {
                // Si no hay datos, podemos mostrar un mensaje en el centro del gráfico
                graficoCircular.centerText = "Sin gastos este mes"
                graficoCircular.data = null
                graficoCircular.invalidate()
            }
        }

        // OBSERVADOR 3: Lista de movimientos recientes
        viewModel.movimientosResult.observe(viewLifecycleOwner) { lista ->
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
            input.inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

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
     * Lógica de representación visual del gráfico de tarta.
     * Transforma la lista de gastos por categoría en entradas para la librería MPAndroidChart.
     */
    private fun actualizarGrafico(lista: List<GastoPorCategoria>) {
        val graficoCircular = view?.findViewById<PieChart>(R.id.graficoCircular) ?: return

        val entradas = mutableListOf<PieEntry>()
        val colores = mutableListOf<Int>()

        lista.forEach { item ->
            // Creamos la porción
            entradas.add(PieEntry(item.totalGasto.toFloat(), item.nombreCategoria))

            try {
                colores.add(Color.parseColor(item.color))
            } catch (e: Exception) {
                colores.add(Color.LTGRAY) // Color por defecto si el hex falla
            }
        }

        // Configuración del set de datos
        val dataSet = PieDataSet(entradas, "")
        dataSet.colors = colores
        dataSet.valueTextSize = 13f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 2f // Espacio entre porciones

        val data = PieData(dataSet)

        // Configuración estética del componente PieChart
        graficoCircular.apply {
            this.data = data
            description.isEnabled = false

            setDrawEntryLabels(false)
            data.setDrawValues(false)

            // Configuración de la Leyenda
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.isWordWrapEnabled = true
            legend.form = Legend.LegendForm.CIRCLE

            // Configuración del Centro Dinámico
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            centerText = "Gastos"
            setCenterTextSize(16f)

            // Interacción: Mostrar info al tocar
            setTouchEnabled(true)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val pieEntry = e as PieEntry
                    centerText = "${pieEntry.label}\n${pieEntry.value} €"
                }

                override fun onNothingSelected() {
                    centerText = "Gastos"
                }
            })

            // Animación de entrada cada vez que se cargan datos
            animateY(1200, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    /**
     * Ciclo de vida: Carga o refresca los datos cada vez que el fragmento vuelve a ser visible.
     * Esto asegura que si se añadió un gasto en otro fragmento, el Dashboard esté actualizado.
     */
    override fun onResume() {
        super.onResume()
        viewModel.cargarDatosDeUsuario()
        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(true)
    }
}