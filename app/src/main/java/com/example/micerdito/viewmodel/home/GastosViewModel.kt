package com.example.micerdito.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.repositorio.GastosRepository
import kotlinx.coroutines.launch

class GastosViewModel() : ViewModel() {

    private val repository = GastosRepository()

    // Lista de categorías que vendrán de la BBDD
    private val _categorias = MutableLiveData<List<Categoria>>()
    val categorias: LiveData<List<Categoria>> = _categorias

    // Categoría que el usuario pulsa en el RecyclerView
    private val _categoriaSeleccionada = MutableLiveData<Categoria?>()
    val categoriaSeleccionada: LiveData<Categoria?> = _categoriaSeleccionada

    // Mensajes de error por si falla la conexión
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        cargarCategorias()
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            val result = repository.obtenerCategorias()
            result.onSuccess { lista ->
                _categorias.value = lista
            }.onFailure { e ->
                _error.value = "Error al cargar categorías: ${e.message}"
            }
        }
    }

    fun seleccionarCategoria(categoria: Categoria?) {
        _categoriaSeleccionada.value = categoria
    }
}