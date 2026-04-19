package com.example.micerdito.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
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
 * FRAGMENTO - HomeFragment
 * Actúa como el centro de control principal del usuario.
 * Muestra el resumen financiero del mes actual, el estado del presupuesto
 * y el historial de transacciones recientes mediante una arquitectura reactiva.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    // Conexión con el ViewModel para gestionar los datos
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculación de los componentes de la vista
        val tvGasto = view.findViewById<TextView>(R.id.tvTotalSpent)
        val tvLimite = view.findViewById<TextView>(R.id.tvLimitStatus)
        val tvMes = view.findViewById<TextView>(R.id.tvMesActual)
        val tvEstablecerLimite = view.findViewById<TextView>(R.id.tvEstablecerLimite)
        val graficoCircular = view.findViewById<PieChart>(R.id.graficoCircular)
        val rvGastos = view.findViewById<RecyclerView>(R.id.rvGastos)

        setupObservers(tvGasto, tvLimite, tvMes, rvGastos, graficoCircular)
        setupListeners(tvEstablecerLimite)
    }

    /**
     * Implementa el patrón Observer para reaccionar dinámicamente a los cambios en los datos.
     */
    private fun setupObservers(
        tvGasto: TextView,
        tvLimite: TextView,
        tvMes: TextView,
        rvGastos: RecyclerView,
        graficoCircular: PieChart
    ) {

        // Sincronización de los datos globales del mes actual
        viewModel.homeResult.observe(viewLifecycleOwner) { data ->
            tvGasto.text = "${String.format("%.2f", data.totalDineroGastado)} €"
            tvLimite.text = "Límite: ${String.format("%.2f", data.limiteMes)} €"
            tvMes.text = data.mesActual.uppercase()
        }

        // Configuración y despliegue del gráfico analítico
        viewModel.graficoResult.observe(viewLifecycleOwner) { lista ->
            if (!lista.isNullOrEmpty()) {
                actualizarGrafico(lista)
            } else {
                // Estado vacío de seguridad si no existen registros
                graficoCircular.centerText = "Sin gastos este mes"
                graficoCircular.data = null
                graficoCircular.invalidate()
            }
        }

        // Población de la lista de transacciones recientes
        viewModel.movimientosResult.observe(viewLifecycleOwner) { lista ->
            if (!lista.isNullOrEmpty()) {
                rvGastos.adapter = MovimientosAdapter(lista)
            }
        }

        /**
         * LÓGICA SEMÁNTICA VISUAL:
         * Altera la coloración de la métrica principal para advertir sobre
         * el estado del presupuesto frente al gasto acumulado.
         */
        viewModel.islimiteSuperado.observe(viewLifecycleOwner) { superado ->
            tvGasto.setTextColor(if (superado) Color.RED else Color.parseColor("#4CAF50"))
        }
    }

    /**
     * Configuración del enrutamiento de eventos generados por el usuario.
     */
    private fun setupListeners(tvEstablecerLimite: TextView) {

        /**
         * Despliega un cuadro de diálogo nativo que permite al usuario
         * redefinir su umbral financiero máximo mensual.
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
                        // Conversión segura para evitar fallos por formato incorrecto
                        nuevoLimite.toDoubleOrNull()?.let { valorNumerico ->
                            viewModel.actualizarLimiteMensual(valorNumerico)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    /**
     * Prepara e inicializa el motor de renderizado de la gráfica circular.
     */
    private fun actualizarGrafico(lista: List<GastoPorCategoria>) {
        val graficoCircular = view?.findViewById<PieChart>(R.id.graficoCircular) ?: return

        val entradas = mutableListOf<PieEntry>()
        val colores = mutableListOf<Int>()

        // Consulta del color de texto adecuado según la tematización activa (Claro/Oscuro)
        val colorTexto = ContextCompat.getColor(requireContext(), R.color.texto_negro)

        lista.forEach { item ->
            entradas.add(PieEntry(item.totalGasto.toFloat(), item.nombreCategoria))
            try {
                colores.add(Color.parseColor(item.color))
            } catch (e: Exception) {
                colores.add(Color.LTGRAY)
            }
        }

        val dataSet = PieDataSet(entradas, "")
        dataSet.colors = colores
        dataSet.valueTextSize = 13f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 2f

        val data = PieData(dataSet)

        graficoCircular.apply {
            this.data = data
            description.isEnabled = false

            setDrawEntryLabels(false)
            data.setDrawValues(false)

            // Configuración de la Leyenda descriptiva
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.isWordWrapEnabled = true
            legend.form = Legend.LegendForm.CIRCLE
            legend.textColor = colorTexto

            // Configuración del núcleo central
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            centerText = "Gastos"
            setCenterTextSize(16f)
            setCenterTextColor(colorTexto)

            // Interacción: Formateo de métricas al seleccionar un segmento
            setTouchEnabled(true)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val pieEntry = e as PieEntry
                    // Aplicación estricta de formato a dos decimales
                    centerText = "${pieEntry.label}\n${String.format("%.2f", pieEntry.value)} €"
                    setCenterTextColor(colorTexto)
                }

                override fun onNothingSelected() {
                    centerText = "Gastos"
                    setCenterTextColor(colorTexto)
                }
            })

            animateY(1200, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    /**
     * Sincroniza el estado de la vista con el servidor y reactiva elementos
     * de la interfaz superior cada vez que el fragmento recupera el foco.
     */
    override fun onResume() {
        super.onResume()
        viewModel.cargarDatosDeUsuario()

        // Habilita la cabecera principal de navegación
        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(true)
    }
}