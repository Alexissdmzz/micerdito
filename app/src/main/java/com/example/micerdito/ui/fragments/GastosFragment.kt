package com.example.micerdito.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.ui.adapters.CategoriaAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización de componentes de la vista
        val rvCategorias = view.findViewById<RecyclerView>(R.id.rvCategorias)
        val cardDetalles = view.findViewById<MaterialCardView>(R.id.cardDetallesGasto)
        val btnCerrar = view.findViewById<ImageButton>(R.id.btnCerrarFormulario)
        val tvTituloFormulario = view.findViewById<TextView>(R.id.etTitulo)
        val etImporte = view.findViewById<EditText>(R.id.etImporte)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)
        val btnGuardarGasto = view.findViewById<Button>(R.id.btnGuardarGasto)

        // Configuración básica del RecyclerView
        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 5)

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers(rvCategorias, cardDetalles, etImporte, etDescripcion)

        // Configuración de interraciones
        setupListeners(btnCerrar, tvTituloFormulario, btnGuardarGasto, etImporte, etDescripcion)
    }

    /**
     * Observadores de estado: Reaccionan a los cambios en el flujo de datos.
     */
    private fun setupObservers(rvCategorias: RecyclerView, cardDetalles: MaterialCardView, etImporte: EditText, etDescripcion: EditText) {
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
    private fun setupListeners(btnCerrar: ImageButton, tvTituloFormulario: TextView, btnGuardarGasto: Button, etImporte: EditText, etDescripcion: EditText) {
        // Botón para cancelar la operación y ocultar el formulario
        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }

        /**
         * Envío del gasto:
         * Valida el importe y formatea la fecha actual antes de llamar al ViewModel.
         */
        btnGuardarGasto.setOnClickListener {
            val importeStr = etImporte.text.toString()
            val tituloGasto = tvTituloFormulario.text.toString()
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
                    descripcion = if (descripcion.isEmpty()) null else descripcion
                )
            } else {
                // Validación visual de error si el campo está vacío
                etImporte.error = "Introduce un importe"
            }
        }
    }
}