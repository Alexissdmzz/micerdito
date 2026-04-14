package com.example.micerdito.ui.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.example.micerdito.utils.parsePositiveAmount
import com.example.micerdito.viewmodel.home.GastosViewModel
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * FRAGMENTO - GastosFragment:
 * Gestiona la interfaz para la creación de nuevos registros de gastos.
 * Utiliza un diseño de cuadrícula para la selección de categorías y un formulario
 * emergente para la introducción de datos monetarios.
 */
class GastosFragment : Fragment(R.layout.fragment_gastos) {

    // Inicialización del ViewModel
    private val viewModel: GastosViewModel by viewModels()

    // Módulo de cámara
    private lateinit var cameraHandler: CameraHandler
    private var fotoUri: Uri? = null

    // Ruta final de la imagen seleccionada o capturada
    private var fotoRuta: String? = null

    // Fecha seleccionada por el usuario para registrar el gasto
    private val calendarioSeleccionado: Calendar = Calendar.getInstance()

    /**
     * Launcher para la captura de imagen con cámara.
     * Si la operación es exitosa, se guarda la ruta y se muestra la previsualización.
     */
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) {
                fotoRuta = cameraHandler.rutaFotoActual
                val ivFoto = view?.findViewById<ImageView>(R.id.ivFotoTicket)
                ivFoto?.setImageURI(fotoUri)
                ivFoto?.visibility = View.VISIBLE
            }
        }

    /**
     * Launcher para seleccionar una imagen desde galería.
     * La imagen se copia a almacenamiento interno para poder trabajar con una ruta local.
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
                        "No se pudo cargar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    /**
     * Launcher para solicitar el permiso de cámara dinámicamente.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            abrirCamaraConSeguridad()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos el gestor de cámara
        cameraHandler = CameraHandler(requireContext())

        // Inicialización de componentes de la vista
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

        // Configuración básica del RecyclerView
        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 4)

        if (rvCategorias.itemDecorationCount == 0) {
            val spacing = resources.getDimensionPixelSize(R.dimen.categoria_spacing)
            rvCategorias.addItemDecoration(ItemDecorator(4, spacing, true))
        }

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers(rvCategorias, cardDetalles, etImporte, etDescripcion, ivFoto)

        // Inicializa el selector visual con la fecha actual
        actualizarTextoFecha(btnFechaGasto)

        // Configuración de interacciones
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
     * Observadores de estado: Reaccionan a los cambios en el flujo de datos.
     */
    private fun setupObservers(
        rvCategorias: RecyclerView,
        cardDetalles: MaterialCardView,
        etImporte: EditText,
        etDescripcion: EditText,
        ivFoto: ImageView
    ) {
        // Carga la lista de categorías obtenidas desde la base de datos MySQL
        viewModel.categorias.observe(viewLifecycleOwner) { lista ->
            rvCategorias.adapter = CategoriaAdapter(lista) { cat ->
                // Acción al pulsar una categoría: se marca como seleccionada en el ViewModel
                viewModel.seleccionarCategoria(cat)
            }
        }

        /**
         * LÓGICA DE VISIBILIDAD DINÁMICA:
         * Si hay una categoría seleccionada, se muestra el formulario de importe.
         * Si es nula, se oculta el formulario y se limpian los campos (Reset).
         */
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

        // Reacción al éxito de la inserción en el servidor PHP
        viewModel.registroExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "¡Gasto guardado! 🐷", Toast.LENGTH_SHORT).show()
                viewModel.seleccionarCategoria(null) // Cerramos el formulario
                viewModel.resetRegistroEstado() // Limpiamos el estado en el ViewModel para evitar duplicados

                // IMPORTANTE: Aquí podrías añadir una navegación al Home
                // o dejar que el usuario siga metiendo gastos.
            }
        }

        // Gestión de mensajes de error de red o validación del servidor
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Configuración de interacciones del usuario.
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
        // Botón para cancelar la operación y ocultar el formulario
        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }

        // ACCIÓN: Mostrar selector con cámara o galería
        btnCamara.setOnClickListener {
            mostrarOpcionesImagen()
        }

        // ACCIÓN: Abrir selector de fecha para el gasto
        btnFechaGasto.setOnClickListener {
            mostrarDatePicker(btnFechaGasto)
        }

        /**
         * Envío del gasto:
         * Valida el importe y formatea la fecha seleccionada antes de llamar al ViewModel.
         */
        btnGuardarGasto.setOnClickListener {
            val importeStr = etImporte.text.toString().trim()
            val tituloGasto = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (tituloGasto.isEmpty()) {
                etTitulo.error = "Introduce un título"
                etTitulo.requestFocus()
                return@setOnClickListener
            }

            if (importeStr.isEmpty()) {
                etImporte.error = "Introduce un importe"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            val importe = parsePositiveAmount(importeStr)
            if (importe == null) {
                etImporte.error = "Introduce un importe válido mayor que 0"
                etImporte.requestFocus()
                return@setOnClickListener
            }

            // Formateo de fecha estándar ISO para compatibilidad con MySQL (DATETIME)
            val fechaSeleccionada = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(calendarioSeleccionado.time)

            viewModel.registrarGasto(
                titulo = tituloGasto,
                importe = importe,
                fecha = fechaSeleccionada,
                descripcion = if (descripcion.isEmpty()) null else descripcion,
                fotoRuta = fotoRuta
            )

        }
    }

    /**
     * Gestión de permisos de cámara para evitar SecurityException.
     */
    private fun verificarPermisosYAbriCamara() {
        if (
            ContextCompat.checkSelfPermission(
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
     * Abre la cámara de forma segura creando previamente la URI de destino.
     */
    private fun abrirCamaraConSeguridad() {
        fotoUri = cameraHandler.generarUriParaCamara()
        fotoUri?.let { uri ->
            camaraLauncher.launch(uri)
        }
    }

    /**
     * Muestra el calendario para que el usuario seleccione la fecha del gasto.
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

    /**
     * Actualiza el texto visible con la fecha seleccionada.
     */
    private fun actualizarTextoFecha(btnFechaGasto: Button) {
        val formatoVisual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = formatoVisual.format(calendarioSeleccionado.time)
        btnFechaGasto.text = "📅 $fecha"
    }

    /**
     * Copia una imagen seleccionada desde galería al almacenamiento de la app.
     * Devuelve la ruta absoluta del archivo generado o null si falla.
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
            e.printStackTrace()
            null
        }
    }

    /**
     * Muestra un diálogo para elegir si la imagen se obtiene desde cámara o galería.
     */
    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Cámara", "Galería")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una opción")
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
        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(false)
    }
}