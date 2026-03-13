package com.example.micerdito.ui.fragments

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CalendarioFragment : Fragment(R.layout.fragment_calendario) {

    private val viewModel: CalendarioViewModel by viewModels()
    private lateinit var gastoAdapter: GastoAdapter
    private lateinit var calendarView: MaterialCalendarView

    private var ultimoMesPedido = -1
    private var ultimoAnioPedido = -1
    private var limiteSeteado: CalendarDay? = null

    private val URL_BASE_IMAGENES = "http://10.202.20.126/micerdito/uploads/tickets/"
    private var uriImagenSeleccionada: Uri? = null

    // Gestiona la selección de imagen y actualiza la vista previa en el diálogo activo
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uriImagenSeleccionada = it
                // Buscamos el ImageView mediante el tag asignado en el BottomSheet
                view?.findViewWithTag<ImageView>("ivTicketActivo")?.setImageURI(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        val pieChartMensual = view.findViewById<PieChart>(R.id.pieChartMensual)
        val rvGastosDia = view.findViewById<RecyclerView>(R.id.rvGastosDia)
        val tvSinDatos = view.findViewById<TextView>(R.id.tvSinDatos)

        gastoAdapter = GastoAdapter()
        rvGastosDia.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gastoAdapter
        }

        calendarView.selectedDate = CalendarDay.today()
        calendarView.setTitleFormatter(MonthArrayTitleFormatter(resources.getTextArray(R.array.meses_espanyol)))
        calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.dias_semana_espanyol)))

        setupObservers(calendarView, pieChartMensual, rvGastosDia, tvSinDatos)
        setupListeners(calendarView, pieChartMensual)
    }

    private fun setupObservers(
        cv: MaterialCalendarView,
        pc: PieChart,
        rv: RecyclerView,
        tv: TextView
    ) {
        viewModel.calendarioData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.success) {
                cv.removeDecorators()
                val decoradores = mutableListOf<DayViewDecorator>()
                var diaRegLocal: CalendarDay? = null

                if (!data.fecha_registro.isNullOrEmpty()) {
                    try {
                        val partes = data.fecha_registro.split(" ")[0].split("-")
                        val diaReg = CalendarDay.from(
                            partes[0].toInt(),
                            partes[1].toInt(),
                            partes[2].toInt()
                        )
                        diaRegLocal = diaReg
                        if (limiteSeteado != diaReg) {
                            limiteSeteado = diaReg
                            cv.state().edit().setMinimumDate(diaReg).commit()
                        }
                        decoradores.add(EventDecorator(Color.BLUE, listOf(diaReg)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (data.dias_con_gastos.isNotEmpty()) {
                    val diasConGastos = data.dias_con_gastos.map {
                        CalendarDay.from(cv.currentDate.year, cv.currentDate.month, it)
                    }.filter { it != diaRegLocal }
                    decoradores.add(EventDecorator(Color.RED, diasConGastos))
                }
                cv.addDecorators(decoradores)

                if (data.resumen_grafico.isNotEmpty()) {
                    pc.visibility = View.VISIBLE
                    actualizarGrafico(pc, data.resumen_grafico)
                } else pc.visibility = View.GONE
            }
        }

        viewModel.gastosDelDia.observe(viewLifecycleOwner) { response ->
            val lista = response?.data ?: emptyList()
            if (lista.isEmpty()) {
                rv.visibility = View.GONE
                if (pc.visibility == View.GONE) tv.visibility = View.VISIBLE
            } else {
                tv.visibility = View.GONE
                rv.visibility = View.VISIBLE
                gastoAdapter.actualizarLista(lista)
            }
        }

        viewModel.accionGastoResult.observe(viewLifecycleOwner) { response ->
            if (response != null && response.success) {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                calendarView.selectedDate?.let {
                    viewModel.obtenerGastosDia(
                        it.year,
                        it.month,
                        it.day
                    )
                }
                viewModel.obtenerDataCalendario(
                    calendarView.currentDate.month,
                    calendarView.currentDate.year
                )
                viewModel.resetAccionGastoResult()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                viewModel.resetError()
            }
        }
    }

    private fun setupListeners(cv: MaterialCalendarView, pc: PieChart) {
        cv.setOnMonthChangedListener { _, date ->
            if (date.month == ultimoMesPedido && date.year == ultimoAnioPedido) return@setOnMonthChangedListener
            ultimoMesPedido = date.month
            ultimoAnioPedido = date.year
            viewModel.obtenerDataCalendario(date.month, date.year)
            gastoAdapter.actualizarLista(emptyList())
        }
        cv.setOnDateChangedListener { _, date, selected ->
            if (selected) viewModel.obtenerGastosDia(date.year, date.month, date.day)
        }
        gastoAdapter.onItemClick = { mostrarBottomSheetDetalle(it) }
    }

    private fun mostrarBottomSheetDetalle(gasto: Gasto) {
        uriImagenSeleccionada = null
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_gasto_detallado_calendario, null)

        val ivTicket = view.findViewById<ImageView>(R.id.ivFotoTicket)
        val btnCambiarFoto = view.findViewById<Button>(R.id.btnCambiarFoto)
        val etTitulo = view.findViewById<EditText>(R.id.etTituloDetalle)
        val etImporte = view.findViewById<EditText>(R.id.etImporteDetalle)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionDetalle)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarGasto)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarCambios)

        ivTicket.tag = "ivTicketActivo"
        etTitulo.setText(gasto.titulo)
        etImporte.setText(gasto.importe.toString())
        etDescripcion.setText(gasto.descripcion ?: "")

        // Carga la foto actual y configura el clic para verla en grande
        if (!gasto.foto_ticket.isNullOrEmpty()) {
            val urlCompleta = URL_BASE_IMAGENES + gasto.foto_ticket
            Glide.with(requireContext()).load(urlCompleta).into(ivTicket)

            ivTicket.setOnClickListener { mostrarFotoGrande(urlCompleta) }
        }

        // El botón específico solo gestiona la nueva selección
        btnCambiarFoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar gasto?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.eliminarGasto(gasto.id_gasto)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null).show()
        }

        btnGuardar.setOnClickListener {
            val nTitulo = etTitulo.text.toString().trim()
            val nImporte = etImporte.text.toString().toDoubleOrNull() ?: 0.0
            val nDesc = etDescripcion.text.toString().trim()

            if (nTitulo.isNotEmpty()) {
                var fotoPart: MultipartBody.Part? = null
                uriImagenSeleccionada?.let { uri ->
                    obtenerFileDesdeUri(requireContext(), uri)?.let { file ->
                        val rb = file.asRequestBody("image/*".toMediaTypeOrNull())
                        fotoPart = MultipartBody.Part.createFormData("foto", file.name, rb)
                    }
                }
                viewModel.editarGasto(gasto.id_gasto, nTitulo, nImporte, nDesc, fotoPart)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun mostrarFotoGrande(url: String) {
        val viewer = ImageView(requireContext())
        Glide.with(requireContext()).load(url).into(viewer)
        AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(viewer)
            .show()
            .window?.decorView?.setOnClickListener { /* cerrar al tocar */ }
    }

    private fun obtenerFileDesdeUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "temp_tk_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun actualizarGrafico(pc: PieChart, lista: List<ResumenCategoria>) {
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
        pc.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setDrawEntryLabels(false)
            legend.isWordWrapEnabled = true
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
        viewModel.obtenerDataCalendario(
            calendarView.currentDate.month,
            calendarView.currentDate.year
        )
    }
}