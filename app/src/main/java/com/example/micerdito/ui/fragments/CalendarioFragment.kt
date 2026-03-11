package com.example.micerdito.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.model.home.ResumenCategoria
import com.example.micerdito.ui.adapters.GastoAdapter
import com.example.micerdito.ui.decorators.EventDecorator
import com.example.micerdito.viewmodel.home.CalendarioViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import java.util.Calendar

/**
 * CalendarioFragment: Gestiona la visualización de actividad financiera mensual y diaria.
 */
class CalendarioFragment : Fragment(R.layout.fragment_calendario) {

    private val viewModel: CalendarioViewModel by viewModels()
    private lateinit var gastoAdapter: GastoAdapter

    private val calendarActual = Calendar.getInstance()
    private val mesActual = calendarActual.get(Calendar.MONTH) + 1
    private val anioActual = calendarActual.get(Calendar.YEAR)

    private var ultimoMesPedido = -1
    private var ultimoAnioPedido = -1
    private var limiteSeteado: CalendarDay? = null

    private lateinit var calendarView: MaterialCalendarView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        val pieChartMensual = view.findViewById<PieChart>(R.id.pieChartMensual)
        val rvGastosDia = view.findViewById<RecyclerView>(R.id.rvGastosDia)
        val tvSinDatos = view.findViewById<TextView>(R.id.tvSinDatos)

        // Configuración del adaptador para los gastos diarios
        gastoAdapter = GastoAdapter()
        rvGastosDia.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gastoAdapter
        }

        calendarView.selectedDate = CalendarDay.today()
        calendarView.setTitleFormatter(MonthArrayTitleFormatter(resources.getTextArray(R.array.meses_espanyol)))
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.dias_semana_espanyol)))

        setupObservers(calendarView, pieChartMensual, rvGastosDia, tvSinDatos)
        setupListeners(calendarView, pieChartMensual, rvGastosDia, tvSinDatos)
    }

    private fun setupObservers(
        calendarView: MaterialCalendarView,
        pieChartMensual: PieChart,
        rvGastosDia: RecyclerView,
        tvSinDatos: TextView
    ) {
        // Observador 1: Resumen mensual (Puntos rojos/azules y gráfico)
        viewModel.calendarioData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.success) {
                calendarView.removeDecorators()
                val decoradores = mutableListOf<DayViewDecorator>()
                var diaRegLocal: CalendarDay? = null

                // Dibujar día de registro (Punto Azul)
                if (!data.fecha_registro.isNullOrEmpty()) {
                    try {
                        val fechaLimpia = data.fecha_registro.split(" ")[0]
                        val partes = fechaLimpia.split("-")
                        val diaReg = CalendarDay.from(
                            partes[0].toInt(),
                            partes[1].toInt(),
                            partes[2].toInt()
                        )
                        diaRegLocal = diaReg

                        if (limiteSeteado != diaReg) {
                            limiteSeteado = diaReg
                            calendarView.state().edit().setMinimumDate(diaReg).commit()
                        }
                        decoradores.add(EventDecorator(Color.BLUE, listOf(diaReg)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Dibujar días con gastos (Puntos Rojos)
                if (data.dias_con_gastos.isNotEmpty()) {
                    val mesVisualizado = calendarView.currentDate.month
                    val anioVisualizado = calendarView.currentDate.year
                    val diasConGastos = data.dias_con_gastos.map { dia ->
                        CalendarDay.from(anioVisualizado, mesVisualizado, dia)
                    }.filter { it != diaRegLocal }
                    decoradores.add(EventDecorator(Color.RED, diasConGastos))
                }
                calendarView.addDecorators(decoradores)

                // --- 2. GRÁFICO (Siempre que haya datos del mes) ---
                if (data.resumen_grafico.isNotEmpty()) {
                    pieChartMensual.visibility = View.VISIBLE
                    // Ya no ocultamos el gráfico aunque sea el mes actual
                    actualizarGrafico(pieChartMensual, data.resumen_grafico)
                } else {
                    pieChartMensual.visibility = View.GONE
                }
            }
        }

        // Observador 2: Gastos del día seleccionado
        viewModel.gastosDelDia.observe(viewLifecycleOwner) { response ->
            // Accedemos a la lista 'data' dentro de GastoResponse
            val lista = response?.data ?: emptyList()

            if (lista.isEmpty()) {
                rvGastosDia.visibility = View.GONE
                if (pieChartMensual.visibility == View.GONE) {
                    tvSinDatos.visibility = View.VISIBLE
                    tvSinDatos.text = "No hay registros este mes 🐷"
                }
            } else {
                tvSinDatos.visibility = View.GONE
                rvGastosDia.visibility = View.VISIBLE
                gastoAdapter.actualizarLista(lista)
            }
        }

        // Observador 3: Errores
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }
    }

    private fun setupListeners(
        calendarView: MaterialCalendarView,
        pieChartMensual: PieChart,
        rvGastosDia: RecyclerView,
        tvSinDatos: TextView
    ) {
        calendarView.setOnMonthChangedListener { _, date ->
            if (date.month == ultimoMesPedido && date.year == ultimoAnioPedido) return@setOnMonthChangedListener

            ultimoMesPedido = date.month
            ultimoAnioPedido = date.year

            viewModel.obtenerDataCalendario(date.month, date.year)
            gastoAdapter.actualizarLista(emptyList()) // Limpiar para evitar datos residuales

            val esMesActual = (date.month == mesActual && date.year == anioActual)
            if (esMesActual) {
                rvGastosDia.visibility = View.VISIBLE
                pieChartMensual.visibility = View.GONE
                tvSinDatos.visibility = View.GONE
            } else {
                rvGastosDia.visibility = View.GONE
            }
        }

        calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                // El ViewModel se encarga de obtener el ID de sesión internamente
                viewModel.obtenerGastosDia(date.year, date.month, date.day)
            }
        }
    }

    private fun actualizarGrafico(graficoCircular: PieChart, lista: List<ResumenCategoria>) {
        val entradas = lista.map { PieEntry(it.total.toFloat(), it.nombre) }
        val colores = lista.map {
            try {
                Color.parseColor(it.color)
            } catch (e: Exception) {
                Color.LTGRAY
            }
        }

        val dataSet = PieDataSet(entradas, "").apply {
            this.colors = colores
            valueTextSize = 13f
            valueTextColor = Color.WHITE
            sliceSpace = 2f
        }

        graficoCircular.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setDrawEntryLabels(false)
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                isWordWrapEnabled = true
            }
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            centerText = "Resumen Mensual"
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    centerText = "${(e as PieEntry).label}\n${e.value} €"
                }

                override fun onNothingSelected() {
                    centerText = "Resumen Mensual"
                }
            })
            animateY(1200, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        val mes = calendarView.currentDate.month
        val anio = calendarView.currentDate.year
        ultimoMesPedido = mes
        ultimoAnioPedido = anio
        viewModel.obtenerDataCalendario(mes, anio)
    }
}