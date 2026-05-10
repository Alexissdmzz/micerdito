package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.AjustesResponse
import com.example.micerdito.data.repositorio.AjustesRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - AjustesViewModel
 * Gestiona la lógica de negocio de la pantalla de configuración. Coordina la actualización de
 * las preferencias locales de la interfaz y la persistencia de cambios de perfil en el servidor remoto.
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los orígenes de datos: remoto (Ajustes) y local (Sesión)
    private val repository = AjustesRepository()
    private val sesionRepository = SesionRepository(application)

    // Canales de comunicación reactiva observados por la vista
    val ajustesResult = MutableLiveData<AjustesResponse?>()
    val errorMsg = MutableLiveData<String>()

    /**
     * VARIABLES DE ESTADO:
     * 'ultimaAccion' ayuda al controlador de la vista a discernir qué proceso ha terminado.
     * 'nombreTemporal' retiene el nuevo alias en memoria hasta que el servidor confirma la mutación.
     */
    var ultimaAccion: String = ""
    var nombreTemporal: String = ""

    // ==========================================
    // GESTIÓN DE PREFERENCIAS LOCALES
    // ==========================================

    fun esModoOscuro(): Boolean = sesionRepository.esModoOscuro()

    fun setModoOscuro(valor: Boolean) {
        sesionRepository.setModoOscuro(valor)
    }

    fun cerrarSesion() {
        sesionRepository.cerrarSesion()
    }

    // ==========================================
    // OPERACIONES DE RED
    // ==========================================

    /**
     * OPERACIÓN: Borrado de Cuenta.
     * Ejecuta una corrutina para solicitar la eliminación permanente del registro en la base de datos.
     */
    fun borrarCuenta() {
        ultimaAccion = "BORRAR"
        val idUsuario = sesionRepository.getIdUsuario()

        viewModelScope.launch {
            val result = repository.eliminarUsuario(idUsuario)

            result.onSuccess {
                ajustesResult.value = it
            }.onFailure { e ->
                errorMsg.value = "Error al eliminar cuenta: ${e.message}"
            }
        }
    }

    /**
     * OPERACIÓN: Edición de Perfil.
     * Sincroniza el nuevo identificador público en el servidor y, tras confirmarse el éxito,
     * actualiza la caché de la sesión local.
     */
    fun editarUsuario(nuevoNombre: String) {
        ultimaAccion = "EDITAR"
        nombreTemporal = nuevoNombre
        val idUsuario = sesionRepository.getIdUsuario()

        viewModelScope.launch {
            val result = repository.editarNombreUsuario(idUsuario, nuevoNombre)

            result.onSuccess {
                ajustesResult.value = it
                sesionRepository.actualizarNombre(nuevoNombre)
            }.onFailure { e ->
                errorMsg.value = "Error al editar nombre: ${e.message}"
            }
        }
    }

    /**
     * Mantenimiento de estado:
     * Purga los resultados anteriores para evitar que la interfaz procese eventos antiguos
     * y muestre notificaciones duplicadas al recrearse el fragmento.
     */
    fun limpiarResultado() {
        ajustesResult.value = null
        ultimaAccion = ""
    }

    fun limpiarError() {
        errorMsg.value = ""
    }
}