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
 * VIEWMODEL - GastosViewModel:
 * Responsable de la lógica de negocio para la creación de gastos.
 * Gestiona el catálogo de categorías, la selección actual del usuario y el proceso
 * de inserción asíncrona en la base de datos MySQL.
 */
class GastosViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios para separar la persistencia remota de la local
    private val repository = GastosRepository()
    private val sesionRepository = SesionRepository(application)

    // LiveData: Canales de comunicación reactiva
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

    // Al instanciar el ViewModel, cargamos automáticamente las categorías disponibles
    init {
        cargarCategorias()
    }

    /**
     * Carga el catálogo de categorías (Iconos y Nombres) desde el servidor.
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
     * Actualiza el estado de la categoría elegida por el usuario.
     * Esto dispara automáticamente la visibilidad del formulario en el Fragment.
     */
    fun seleccionarCategoria(categoria: Categoria?) {
        _categoriaSeleccionada.value = categoria
    }

    /**
     * OPERACIÓN: Inserción de Gasto
     * * Coordina los datos de sesión (ID Usuario), los datos de selección (ID Categoría)
     * y los datos de entrada del formulario para realizar la petición POST.
     */
    fun registrarGasto(titulo: String, importe: Double, fecha: String, descripcion: String?) {
        // Obtenemos el ID del usuario de la persistencia local (SharedPreferences)
        val idUser = sesionRepository.getIdUsuario()
        // Recuperamos el ID de la categoría seleccionada previamente en la UI
        val idCat = _categoriaSeleccionada.value?.idCategoria

        if (idCat == null) {
            _error.value = "Por favor, selecciona una categoría"
            return
        }

        _isLoading.value = true

        // Lanzamiento de corrutina en el ámbito del ViewModel
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

    /**
     * Método de limpieza para resetear el flag de éxito tras la navegación.
     */
    fun resetRegistroEstado() {
        _registroExitoso.value = false
    }
}