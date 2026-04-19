package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.repositorio.GastosRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - GastosViewModel
 * Responsable de la lógica de negocio para la creación de nuevas transacciones.
 * Gestiona la carga del catálogo de categorías, el estado de selección de la interfaz
 * y la orquestación de la persistencia asíncrona de datos y comprobantes multimedia.
 */
class GastosViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los orígenes de datos: remoto y local
    private val repository = GastosRepository()
    private val sesionRepository = SesionRepository(application)

    // Canales de estado y comunicación reactiva con encapsulamiento estricto (Backing Properties)
    private val _categorias = MutableLiveData<List<Categoria>>()
    val categorias: LiveData<List<Categoria>> = _categorias

    private val _categoriaSeleccionada = MutableLiveData<Categoria?>()
    val categoriaSeleccionada: LiveData<Categoria?> = _categoriaSeleccionada

    private val _registroExitoso = MutableLiveData<Boolean>()
    val registroExitoso: LiveData<Boolean> = _registroExitoso

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Inicialización proactiva del catálogo al instanciar el componente
    init {
        cargarCategorias()
    }

    /**
     * Extrae el catálogo maestro de clasificación de gastos desde el servidor
     * para alimentar la cuadrícula visual de la interfaz.
     */
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

    /**
     * Actualiza el estado lógico de la categoría elegida por el usuario.
     * Esta mutación altera dinámicamente la visibilidad del formulario en la capa de presentación.
     */
    fun seleccionarCategoria(categoria: Categoria?) {
        _categoriaSeleccionada.value = categoria
    }

    /**
     * OPERACIÓN: Registro de Transacción.
     * Ensambla la identidad del usuario, la clasificación seleccionada y los parámetros financieros
     * para conformar y emitir la carga útil hacia el repositorio.
     */
    fun registrarGasto(
        titulo: String,
        importe: Double,
        fecha: String,
        descripcion: String?,
        fotoRuta: String?
    ) {
        val idUser = sesionRepository.getIdUsuario()
        val idCat = _categoriaSeleccionada.value?.idCategoria

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
                descripcion = descripcion ?: "",
                fotoRuta = fotoRuta
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

    /**
     * Mantenimiento de estado: Reinicia el indicador de éxito tras ser consumido por la vista
     * para prevenir reenvíos accidentales o problemas en la navegación.
     */
    fun resetRegistroEstado() {
        _registroExitoso.value = false
    }
}