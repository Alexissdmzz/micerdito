package com.example.micerdito.ui.fragments

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.micerdito.BuildConfig
import com.example.micerdito.R
import com.example.micerdito.data.model.home.Gasto
import com.example.micerdito.data.model.home.ResumenCategoria
import com.example.micerdito.ui.adapters.GastoAdapter
import com.example.micerdito.ui.decorators.EventDecorator
import com.example.micerdito.ui.handlers.CameraHandler
import com.example.micerdito.utils.ValidationUtils
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

/**
 * FRAGMENTO - CalendarioFragment
 * Controlador encargado de la representación temporal de la actividad financiera.
 * Gestiona el calendario interactivo, la gráfica de desglose mensual y el panel
 * de edición detallada de transacciones, incluyendo captura multimedia.
 */
class CalendarioFragment : Fragment(R.layout.fragment_calendario) {

    // TAG de clase: identifica la fuente del log sin revelar lógica interna
    private val TAG = "CalendarioFragment"

    private val viewModel: CalendarioViewModel by viewModels()
    private lateinit var gastoAdapter: GastoAdapter
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var cameraHandler: CameraHandler

    private var ultimoMesPedido = -1
    private var ultimoAnioPedido = -1
    private var limiteSeteado: CalendarDay? = null

    private val URL_BASE_IMAGENES = BuildConfig.BASE_URL + "/micerdito_api/uploads/tickets/"
    private var uriImagenSeleccionada: Uri? = null
    private var ivTicketEdicion: ImageView? = null
    private var borrarFotoPendiente = false

    /**
     * Componente nativo para gestionar la respuesta de la aplicación de cámara.
     */
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraHandler.rutaFotoActual?.let { ruta ->
                    val uri = Uri.fromFile(File(ruta))
                    actualizarImagenEnDialogo(uri)
                }
            }
        }

    /**
     * Componente nativo para gestionar la selección de archivos desde el almacenamiento local.
     */
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { actualizarImagenEnDialogo(it) }
        }

    /**
     * Actualiza la vista previa del comprobante en el panel de edición
     * y habilita la visualización a pantalla completa.
     */
    private fun actualizarImagenEnDialogo(uri: Uri) {
        uriImagenSeleccionada = uri
        borrarFotoPendiente = false
        ivTicketEdicion?.setImageURI(uri)
        ivTicketEdicion?.setOnClickListener { mostrarFotoGrande(uri) }

        ivTicketEdicion?.rootView?.findViewById<Button>(R.id.btnEliminarFoto)?.visibility =
            View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraHandler = CameraHandler(requireContext())
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
        calendarView.setHeaderTextAppearance(R.style.CalendarHeaderText)
        calendarView.setWeekDayTextAppearance(R.style.CalendarWeekDayText)
        calendarView.setDateTextAppearance(R.style.CalendarDayText)

        setupObservers(calendarView, pieChartMensual, rvGastosDia, tvSinDatos)
        setupListeners(calendarView, pieChartMensual)
    }

    /**
     * Implementa el patrón Observer para mantener sincronizada la UI con
     * el estado de la base de datos (eventos, listados y gráficas).
     */
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

                val colorAzul = ContextCompat.getColor(requireContext(), R.color.accent_blue)
                val colorRojo = ContextCompat.getColor(requireContext(), R.color.error_red)

                if (!data.fechaRegistro.isNullOrEmpty()) {
                    try {
                        val partes = data.fechaRegistro.split(" ")[0].split("-")
                        val diaReg = CalendarDay.from(
                            partes[0].toInt(),
                            partes[1].toInt(),
                            partes[2].toInt()
                        )
                        diaRegLocal = diaReg

                        if (limiteSeteado != diaReg) {
                            limiteSeteado = diaReg
                        }
                        decoradores.add(EventDecorator(colorAzul, listOf(diaReg)))
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al parsear fecha de registro del calendario", e)
                    }
                }

                if (data.diasConGastos.isNotEmpty()) {
                    val diasConGastos = data.diasConGastos.map {
                        CalendarDay.from(cv.currentDate.year, cv.currentDate.month, it)
                    }.filter { it != diaRegLocal }
                    decoradores.add(EventDecorator(colorRojo, diasConGastos))
                }
                cv.addDecorators(decoradores)

                if (data.resumenGrafico.isNotEmpty()) {
                    pc.visibility = View.VISIBLE
                    actualizarGrafico(pc, data.resumenGrafico)
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
                Toast.makeText(requireContext(), "Operación realizada correctamente", Toast.LENGTH_SHORT).show()

                val diaRefresco = calendarView.selectedDate ?: CalendarDay.today()
                viewModel.obtenerGastosDia(diaRefresco.year, diaRefresco.month, diaRefresco.day)

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

    /**
     * Despliega un panel inferior interactivo que permite visualizar y alterar
     * los detalles de un movimiento financiero preexistente.
     */
    private fun mostrarBottomSheetDetalle(gasto: Gasto) {
        uriImagenSeleccionada = null
        borrarFotoPendiente = false
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.item_gasto_detallado_calendario, null)

        val ivTicket = view.findViewById<ImageView>(R.id.ivFotoTicket)
        val btnCambiarFoto = view.findViewById<Button>(R.id.btnCambiarFoto)
        val btnEliminarFoto = view.findViewById<Button>(R.id.btnEliminarFoto)
        val etTitulo = view.findViewById<EditText>(R.id.etTituloDetalle)
        val etImporte = view.findViewById<EditText>(R.id.etImporteDetalle)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcionDetalle)
        val btnEliminar = view.findViewById<Button>(R.id.btnEliminarGasto)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarCambios)

        ivTicketEdicion = ivTicket
        etTitulo.setText(gasto.titulo)
        etImporte.setText(String.format(java.util.Locale.US, "%.2f", gasto.importe))
        etDescripcion.setText(gasto.descripcion ?: "")

        if (!gasto.fotoTicket.isNullOrEmpty()) {
            // Verificación del formato de la URL (Absoluta vs Relativa)
            val urlFinal = if (gasto.fotoTicket.startsWith("http")) {
                gasto.fotoTicket
            } else {
                URL_BASE_IMAGENES + gasto.fotoTicket
            }

            Glide.with(requireContext())
                .load(urlFinal)
                .signature(ObjectKey(System.currentTimeMillis()))
                .error(R.drawable.ic_sin_foto)
                .into(ivTicket)

            btnEliminarFoto.visibility = View.VISIBLE
            ivTicket.setOnClickListener { mostrarFotoGrande(urlFinal) }
        } else {
            ivTicket.setImageResource(R.drawable.ic_sin_foto)
            btnEliminarFoto.visibility = View.GONE
        }

        btnCambiarFoto.setOnClickListener {
            val opciones = arrayOf("Cámara", "Galería")
            AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar origen")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> cameraHandler.generarUriParaCamara()?.let { cameraLauncher.launch(it) }
                        1 -> pickImageLauncher.launch("image/*")
                    }
                }.show()
        }

        btnEliminarFoto.setOnClickListener {
            borrarFotoPendiente = true
            uriImagenSeleccionada = null
            ivTicket.setImageResource(R.drawable.ic_sin_foto)
            btnEliminarFoto.visibility = View.GONE
            ivTicket.setOnClickListener(null)
        }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar gasto?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.eliminarGasto(gasto.idGasto)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null).show()
        }

        btnGuardar.setOnClickListener {
            val nTitulo = etTitulo.text.toString().trim()
            val nImporteTexto = etImporte.text.toString().trim()
            val nDesc = etDescripcion.text.toString().trim()

            if (nTitulo.isEmpty()) {
                etTitulo.error = "El título es obligatorio"
                etTitulo.requestFocus()
                return@setOnClickListener
            }

            if (nImporteTexto.isEmpty()) {
                etImporte.error = "El importe es obligatorio"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            val nImporte = ValidationUtils.parsePositiveAmount(nImporteTexto)
            if (nImporte == null) {
                etImporte.error = "Introduce un importe válido mayor que 0"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            var fotoPart: MultipartBody.Part? = null
            uriImagenSeleccionada?.let { uri ->
                obtenerFileDesdeUri(requireContext(), uri)?.let { file ->
                    val rb = file.asRequestBody("image/*".toMediaTypeOrNull())
                    fotoPart = MultipartBody.Part.createFormData("foto", file.name, rb)
                }
            }

            val fotoActualParam = when {
                uriImagenSeleccionada != null -> ""
                borrarFotoPendiente -> ""
                else -> gasto.fotoTicket ?: ""
            }

            viewModel.editarGasto(
                gasto.idGasto, nTitulo, nImporte, nDesc, fotoActualParam, fotoPart
            )
            dialog.dismiss()
        }
        dialog.setOnDismissListener { ivTicketEdicion = null }
        dialog.setContentView(view)
        dialog.show()
    }

    /**
     * Genera una superposición de pantalla completa para visualizar
     * el comprobante multimedia sin restricciones de tamaño.
     */
    private fun mostrarFotoGrande(origen: Any) {
        val dialog =
            android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val root = android.widget.FrameLayout(requireContext())

        val visor = ImageView(requireContext()).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }

        val btnCerrar = Button(requireContext()).apply {
            text = "Cerrar"
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 120)
            }
            setOnClickListener { dialog.dismiss() }
        }

        root.addView(visor)
        root.addView(btnCerrar)
        dialog.setContentView(root)

        Glide.with(this)
            .load(origen)
            .signature(ObjectKey(System.currentTimeMillis()))
            .error(R.drawable.ic_sin_foto)
            .into(visor)
        dialog.show()
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
            Log.e(TAG, "Error al copiar archivo desde URI", e)
            null
        }
    }

    /**
     * Configura el motor de renderizado de la gráfica de tipo pastel.
     * Incorpora formato de precisión de dos decimales para los importes.
     */
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

        val colorTexto = ContextCompat.getColor(requireContext(), R.color.texto_negro)

        pc.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setDrawEntryLabels(false)

            legend.isWordWrapEnabled = true
            legend.textColor = colorTexto

            centerText = "Resumen Mensual"
            setCenterTextColor(colorTexto)

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)

            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL

            data.setDrawValues(false)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // Formateo estricto a dos decimales para la visualización central de la gráfica
                    centerText = "${(e as PieEntry).label}\n${String.format("%.2f", e.value)} €"
                    setCenterTextColor(colorTexto)
                }

                override fun onNothingSelected() {
                    centerText = "Resumen Mensual"
                    setCenterTextColor(colorTexto)
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

        val diaActual = calendarView.selectedDate ?: CalendarDay.today()
        viewModel.obtenerGastosDia(diaActual.year, diaActual.month, diaActual.day)

        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(false)
    }
}