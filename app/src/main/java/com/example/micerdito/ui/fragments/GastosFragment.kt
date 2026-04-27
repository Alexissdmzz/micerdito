package com.example.micerdito.ui.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.ui.adapters.CategoriaAdapter
import com.example.micerdito.ui.decorators.ItemDecorator
import com.example.micerdito.ui.handlers.CameraHandler
import com.example.micerdito.utils.ValidationUtils
import com.example.micerdito.viewmodel.home.GastosViewModel
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FRAGMENTO - GastosFragment
 * Interfaz dedicada al registro de nuevas transacciones.
 * Integra un sistema de selección visual mediante cuadrícula, captura multimedia
 * para comprobantes y validación estricta de datos financieros.
 */
class GastosFragment : Fragment(R.layout.fragment_gastos) {

    private val TAG = "GastosFragment"

    // Conexión con el ViewModel para gestionar la lógica de negocio
    private val viewModel: GastosViewModel by viewModels()

    // Gestor de hardware para la captura de imágenes
    private lateinit var cameraHandler: CameraHandler
    private var fotoUri: Uri? = null

    // Referencia absoluta de la imagen seleccionada en el almacenamiento local
    private var fotoRuta: String? = null

    // Estado temporal de la fecha seleccionada para la transacción
    private val calendarioSeleccionado: Calendar = Calendar.getInstance()

    /**
     * Componente nativo para gestionar la captura de fotografías.
     * Vincula el resultado de la cámara con la vista previa de la interfaz.
     */
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                fotoRuta = cameraHandler.rutaFotoActual
                val ivFoto = view?.findViewById<ImageView>(R.id.ivFotoTicket)
                ivFoto?.setImageURI(fotoUri)
                ivFoto?.visibility = View.VISIBLE
            }
        }

    /**
     * Componente nativo para la selección de archivos del sistema.
     * Garantiza el acceso seguro copiando el archivo al espacio privado de la aplicación.
     */
    private val galeriaLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uriSeleccionada ->
            uriSeleccionada?.let {
                val rutaLocal = copiarImagenAGuardadoInterno(it)
                if (rutaLocal != null) {
                    fotoRuta = rutaLocal
                    view?.findViewById<ImageView>(R.id.ivFotoTicket)?.apply {
                        setImageURI(uriSeleccionada)
                        visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al procesar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    /**
     * Componente nativo para solicitar permisos de hardware en tiempo de ejecución.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            abrirCamaraConSeguridad()
        } else {
            Toast.makeText(requireContext(), "Se requiere acceso a la cámara", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraHandler = CameraHandler(requireContext())

        // Vinculación de los componentes de la vista
        val rvCategorias = view.findViewById<RecyclerView>(R.id.rvCategorias)
        val cardDetalles = view.findViewById<MaterialCardView>(R.id.cardDetallesGasto)
        val btnCerrar = view.findViewById<ImageButton>(R.id.btnCerrarFormulario)
        val etTitulo = view.findViewById<EditText>(R.id.etTitulo)
        val etImporte = view.findViewById<EditText>(R.id.etImporte)
        val btnFechaGasto = view.findViewById<Button>(R.id.btnFechaGasto)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnGuardarGasto = view.findViewById<Button>(R.id.btnGuardarGasto)
        val btnCamara = view.findViewById<Button>(R.id.btnSubirFactura)
        val ivFoto = view.findViewById<ImageView>(R.id.ivFotoTicket)

        // Configuración de la cuadrícula de categorías
        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 4)
        if (rvCategorias.itemDecorationCount == 0) {
            val spacing = resources.getDimensionPixelSize(R.dimen.categoria_spacing)
            rvCategorias.addItemDecoration(ItemDecorator(4, spacing, true))
        }

        setupObservers(rvCategorias, cardDetalles, etImporte, etDescripcion, ivFoto)
        actualizarTextoFecha(btnFechaGasto)
        setupListeners(
            btnCerrar,
            btnCamara,
            etTitulo,
            btnGuardarGasto,
            etImporte,
            etDescripcion,
            btnFechaGasto
        )
    }

    /**
     * Implementa el patrón Observer para reaccionar a la lógica de selección y guardado.
     */
    private fun setupObservers(
        rvCategorias: RecyclerView,
        cardDetalles: MaterialCardView,
        etImporte: EditText,
        etDescripcion: EditText,
        ivFoto: ImageView
    ) {

        // Pinta el catálogo de categorías disponibles
        viewModel.categorias.observe(viewLifecycleOwner) { lista ->
            rvCategorias.adapter = CategoriaAdapter(lista) { cat ->
                viewModel.seleccionarCategoria(cat)
            }
        }

        // Control dinámico de la visibilidad del formulario secundario
        viewModel.categoriaSeleccionada.observe(viewLifecycleOwner) { cat ->
            if (cat != null) {
                cardDetalles.visibility = View.VISIBLE
            } else {
                cardDetalles.visibility = View.GONE
                etImporte.text.clear()
                etDescripcion.text.clear()
                ivFoto.visibility = View.GONE
            }
        }

        // Gestión de la respuesta del servidor tras enviar la transacción
        viewModel.registroExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(
                    requireContext(),
                    "Gasto registrado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.seleccionarCategoria(null)
                viewModel.resetRegistroEstado()
            }
        }

        // Captura y exposición de errores de red o validación
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Configuración del enrutamiento de eventos generados por el usuario.
     */
    private fun setupListeners(
        btnCerrar: ImageButton,
        btnCamara: Button,
        etTitulo: EditText,
        btnGuardarGasto: Button,
        etImporte: EditText,
        etDescripcion: EditText,
        btnFechaGasto: Button
    ) {

        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }

        btnCamara.setOnClickListener {
            mostrarOpcionesImagen()
        }

        btnFechaGasto.setOnClickListener {
            mostrarDatePicker(btnFechaGasto)
        }

        // Proceso de validación local y envío de la carga útil al servidor
        btnGuardarGasto.setOnClickListener {
            val importeStr = etImporte.text.toString().trim()
            val tituloGasto = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (tituloGasto.isEmpty()) {
                etTitulo.error = "Introduce un concepto"
                etTitulo.requestFocus()
                return@setOnClickListener
            }

            if (importeStr.isEmpty()) {
                etImporte.error = "Introduce un importe"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            val importe = ValidationUtils.parsePositiveAmount(importeStr)
            if (importe == null) {
                etImporte.error = "Introduce un importe válido mayor que 0"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            // Adaptación de la fecha al formato estándar ISO requerido por la base de datos
            val fechaSeleccionada = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(calendarioSeleccionado.time)

            viewModel.registrarGasto(
                titulo = tituloGasto,
                importe = importe,
                fecha = fechaSeleccionada,
                descripcion = descripcion.ifEmpty { null },
                fotoRuta = fotoRuta
            )
        }
    }

    /**
     * Evalúa el estado de los permisos de hardware antes de inicializar la cámara.
     */
    private fun verificarPermisosYAbriCamara() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            abrirCamaraConSeguridad()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Prepara el puntero de almacenamiento seguro y lanza el intent de captura.
     */
    private fun abrirCamaraConSeguridad() {
        fotoUri = cameraHandler.generarUriParaCamara()
        fotoUri?.let { uri ->
            camaraLauncher.launch(uri)
        }
    }

    /**
     * Despliega el componente nativo de selección temporal.
     */
    private fun mostrarDatePicker(btnFechaGasto: Button) {
        val year = calendarioSeleccionado.get(Calendar.YEAR)
        val month = calendarioSeleccionado.get(Calendar.MONTH)
        val day = calendarioSeleccionado.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendarioSeleccionado.set(Calendar.YEAR, selectedYear)
                calendarioSeleccionado.set(Calendar.MONTH, selectedMonth)
                calendarioSeleccionado.set(Calendar.DAY_OF_MONTH, selectedDay)
                actualizarTextoFecha(btnFechaGasto)
            },
            year,
            month,
            day
        ).show()
    }

    private fun actualizarTextoFecha(btnFechaGasto: Button) {
        val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        btnFechaGasto.text = formatoVisual.format(calendarioSeleccionado.time)
    }

    /**
     * Transfiere el binario de la imagen desde un origen externo al espacio privado de la aplicación.
     */
    private fun copiarImagenAGuardadoInterno(uri: Uri): String? {
        return try {
            val inputStream: InputStream =
                requireContext().contentResolver.openInputStream(uri) ?: return null
            val archivoDestino = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "TICKET_GALERIA_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(archivoDestino).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            archivoDestino.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error al copiar imagen a almacenamiento interno", e)
            null
        }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Cámara", "Galería")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar origen")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisosYAbriCamara()
                    1 -> galeriaLauncher.launch("image/*")
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Oculta la cabecera principal al entrar en la sección de inserción
        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(false)
    }
}