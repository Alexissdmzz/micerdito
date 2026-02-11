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

class GastosFragment : Fragment(R.layout.fragment_gastos) {

    private val viewModel: GastosViewModel by viewModels()
    private lateinit var preferenciasSesion: PreferenciasSesion

    private lateinit var rvCategorias: RecyclerView
    private lateinit var cardDetalles: MaterialCardView
    private lateinit var btnCerrar: ImageButton
    private lateinit var tvTituloFormulario: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenciasSesion = PreferenciasSesion(requireContext())
        initViews(view)
        setupObservers()
        setupListeners()
    }

    private fun initViews(view: View) {
        rvCategorias = view.findViewById(R.id.rvCategorias)
        cardDetalles = view.findViewById(R.id.cardDetallesGasto)
        btnCerrar = view.findViewById(R.id.btnCerrarFormulario)
        tvTituloFormulario = view.findViewById(R.id.etTitulo)

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
                tvTituloFormulario.text = "Añadir a ${cat.nombre}"
                cardDetalles.visibility = View.VISIBLE
            } else {
                cardDetalles.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        btnCerrar.setOnClickListener {
            viewModel.seleccionarCategoria(null)
        }
    }
}