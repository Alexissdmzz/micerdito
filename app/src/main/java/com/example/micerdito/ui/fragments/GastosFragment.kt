package com.example.micerdito.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.micerdito.R
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.adapters.CategoriaAdapter
import com.example.micerdito.viewmodel.home.GastosViewModel
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GastosFragment : Fragment(R.layout.fragment_gastos) {

    private val viewModel: GastosViewModel by viewModels()
    private lateinit var rvCategorias: RecyclerView
    private lateinit var cardDetalles: MaterialCardView
    private lateinit var btnCerrar: ImageButton
    private lateinit var tvTituloFormulario: TextView
    private lateinit var etImporte: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardarGasto: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupObservers()
        setupListeners()
    }

    private fun initViews(view: View) {
        rvCategorias = view.findViewById(R.id.rvCategorias)
        cardDetalles = view.findViewById(R.id.cardDetallesGasto)
        btnCerrar = view.findViewById(R.id.btnCerrarFormulario)

        tvTituloFormulario = view.findViewById(R.id.etTitulo)
        etImporte = view.findViewById(R.id.etImporte)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        btnGuardarGasto = view.findViewById(R.id.btnGuardarGasto)

        rvCategorias.layoutManager = GridLayoutManager(requireContext(), 5)
    }

    private fun setupObservers() {
        viewModel.categorias.observe(viewLifecycleOwner) { lista ->
            rvCategorias.adapter = CategoriaAdapter(lista) { cat ->
                viewModel.seleccionarCategoria(cat)
            }
        }

        viewModel.categoriaSeleccionada.observe(viewLifecycleOwner) { cat ->
            if (cat != null) {
                cardDetalles.visibility = View.VISIBLE
                etImporte.requestFocus()
            } else {
                cardDetalles.visibility = View.GONE
                etImporte.text.clear()
                etDescripcion.text.clear()
            }
        }

        viewModel.registroExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "¡Gasto guardado! 🐷", Toast.LENGTH_SHORT).show()
                viewModel.seleccionarCategoria(null) // Cerramos el formulario
                viewModel.resetRegistroEstado() // Limpiamos el estado en el ViewModel

                // IMPORTANTE: Aquí podrías añadir una navegación al Home
                // o dejar que el usuario siga metiendo gastos.
            }
        }

        // Manejar errores
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }

        btnGuardarGasto.setOnClickListener {
            val importeStr = etImporte.text.toString()
            val tituloGasto = tvTituloFormulario.text.toString()
            val descripcion = etDescripcion.text.toString()

            if (importeStr.isNotEmpty()) {
                val importe = importeStr.toDouble()
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
                etImporte.error = "Introduce un importe"
            }
        }
    }
}