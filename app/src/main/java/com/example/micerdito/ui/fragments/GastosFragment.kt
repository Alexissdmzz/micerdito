package com.example.micerdito.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import com.example.micerdito.viewmodel.home.GastosViewModel
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
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

    // Launcher para la captura de imagen
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            val ivFoto = view?.findViewById<ImageView>(R.id.ivFotoTicket)
            ivFoto?.setImageURI(fotoUri)
            ivFoto?.visibility = View.VISIBLE
        }
    }

    // Launcher para solicitar el permiso de cámara dinámicamente
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            abrirCamaraConSeguridad()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
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

        // Configuración de interraciones
        setupListeners(btnCerrar, btnCamara, etTitulo, btnGuardarGasto, etImporte, etDescripcion)
    }

    /**
     * Observadores de estado: Reaccionan a los cambios en el flujo de datos.
     */
    private fun setupObservers(rvCategorias: RecyclerView, cardDetalles: MaterialCardView, etImporte: EditText, etDescripcion: EditText, ivFoto: ImageView) {
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
    private fun setupListeners(btnCerrar: ImageButton, btnCamara: Button,  etTitulo: EditText, btnGuardarGasto: Button, etImporte: EditText, etDescripcion: EditText) {
        // Botón para cancelar la operación y ocultar el formulario
        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }

        // ACCIÓN: Abrir Cámara usando el módulo externo con chequeo de permisos
        btnCamara.setOnClickListener {
            verificarPermisosYAbriCamara()
        }

        /**
         * Envío del gasto:
         * Valida el importe y formatea la fecha actual antes de llamar al ViewModel.
         */
        btnGuardarGasto.setOnClickListener {
            val importeStr = etImporte.text.toString()
            val tituloGasto = etTitulo.text.toString()
            val descripcion = etDescripcion.text.toString()

            if (importeStr.isNotEmpty()) {
                val importe = importeStr.toDouble()

                // Formateo de fecha estándar ISO para compatibilidad con MySQL (DATETIME)
                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                )

                viewModel.registrarGasto(
                    titulo = tituloGasto,
                    importe = importe,
                    fecha = fechaActual,
                    descripcion = if (descripcion.isEmpty()) null else descripcion,
                    fotoRuta = cameraHandler.rutaFotoActual
                )
            } else {
                // Validación visual de error si el campo está vacío
                etImporte.error = "Introduce un importe"
            }
        }
    }

    /**
     * Gestión de permisos de cámara para evitar SecurityException.
     */
    private fun verificarPermisosYAbriCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamaraConSeguridad()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamaraConSeguridad() {
        fotoUri = cameraHandler.generarUriParaCamara()
        fotoUri?.let { uri ->
            camaraLauncher.launch(uri)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? com.example.micerdito.ui.home.HomeActivity)?.mostrarHeader(false)
    }
}