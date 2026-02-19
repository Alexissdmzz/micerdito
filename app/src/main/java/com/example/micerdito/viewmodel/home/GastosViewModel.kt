package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.model.home.GastosResponse
import com.example.micerdito.data.repositorio.GastosRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

class GastosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GastosRepository()
    private val sesionRepository = SesionRepository(application)

    private val _categorias = MutableLiveData<List<Categoria>>()
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _categoriaSeleccionada = MutableLiveData<Categoria?>()
    val categoriaSeleccionada: LiveData<Categoria?> = _categoriaSeleccionada

    // Estado de la inserción
    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> = _registroExitoso

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        cargarCategorias()
    }

    private fun cargarCategorias() {
        viewModelScope.launch {
            val result = repository.obtenerCategorias()
            result.onSuccess { lista ->
                _categorias.value =lista
            }.onFailure { e ->
                _error.value = "Error al cargar categorías: ${e.message}"
            }
        }
    }

    fun seleccionarCategoria(categoria: Categoria?) {
        _categoriaSeleccionada.value = categoria
    }

    /**
     * FUNCIÓN PARA INSERTAR EL GASTO
     */
    fun registrarGasto(titulo: String, importe: Double, fecha: String, descripcion: String?) {
        val idUser = sesionRepository.getIdUsuario()
        val idCat = _categoriaSeleccionada.value?.idCategoria // Extraemos el ID del objeto seleccionado

        if (idCat == null) {
            _error.value = "Por favor, selecciona una categoría"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.insertarGasto(
                idUsuario = idUser,
                idCategoria = idCat,
                titulo = titulo,
                importe = importe,
                fechaGasto = fecha,
                descripcion = descripcion ?: ""
            )

            result.onSuccess { response ->
                if (response.success) {
                    _registroExitoso.value = true
                } else {
                    _error.value = response.message
                }
            }.onFailure { e ->
                _error.value = "Error al guardar el gasto: ${e.message}"
            }

            _isLoading.value = false
        }
    }

    // Resetear el estado después de navegar para que no se repita la acción
    fun resetRegistroEstado() {
        _registroExitoso.value = false
    }
}