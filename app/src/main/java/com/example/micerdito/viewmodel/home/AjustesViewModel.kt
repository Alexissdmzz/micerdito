package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.AjustesResponse
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.data.repositorio.AjustesRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - AjustesViewModel:
 * Gestiona la lógica de la pantalla de configuración. Coordina la actualización de
 * preferencias locales (UI) y la persistencia de cambios de perfil en el servidor remoto.
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios para separar la persistencia remota de la local
    private val repository = AjustesRepository()
    private val sesionRepository = SesionRepository(application)

    // LiveData: Canales de comunicación reactiva
    val ajustesResult = MutableLiveData<AjustesResponse?>()
    val errorMsg = MutableLiveData<String>()

    /**
     * VARIABLES DE ESTADO:
     * 'ultimaAccion' ayuda al Fragment a discernir qué proceso ha terminado (EDITAR o BORRAR).
     * 'nombreTemporal' almacena el cambio antes de confirmarlo en la sesión local.
     */
    var ultimaAccion: String = "" // Declaramos la ultima acción para no solapar acciones
    var nombreTemporal: String =
        "" // Usamos esta variable para almacenar el nombre de la BBDD si se llega a cambiar

    // GESTIÓN DE PREFERENCIAS LOCALES (Síncronas)
    fun esModoOscuro(): Boolean = sesionRepository.esModoOscuro()
    fun setModoOscuro(valor: Boolean) {
        sesionRepository.setModoOscuro(valor)
    }

    fun esDaltonico(): Boolean = sesionRepository.esDaltonico()
    fun setDaltonico(valor: Boolean) {
        sesionRepository.setDaltonico(valor)
    }

    fun cerrarSesion() {
        sesionRepository.cerrarSesion()
    }

    /**
     * OPERACIÓN: Borrado de Cuenta
     * Lanza una corrutina para eliminar permanentemente el registro en MySQL.
     */
    fun borrarCuenta() {
        ultimaAccion = "BORRAR"
        val idUsuario = sesionRepository.getIdUsuario()

        // Lanzamiento de corrutina en el ámbito del ViewModel
        viewModelScope.launch {
            val result = repository.eliminarUsuario(idUsuario)

            result.onSuccess {
                ajustesResult.value = it
            }.onFailure {
                errorMsg.value = it.message
            }
        }

    }

    /**
     * OPERACIÓN: Edición de Perfil
     * Sincroniza el nuevo nombre en el servidor y, tras el éxito,
     * actualiza la sesión local en SharedPreferences.
     */
    fun editarUsuario(nuevoNombre: String) {
        ultimaAccion = "EDITAR"
        nombreTemporal = nuevoNombre
        val idUsuario = sesionRepository.getIdUsuario()

        // Lanzamiento de corrutina en el ámbito del ViewModel
        viewModelScope.launch {
            val result = repository.editarNombreUsuario(idUsuario, nuevoNombre)

            result.onSuccess {
                ajustesResult.value = it
                sesionRepository.actualizarNombre(nuevoNombre)
            }.onFailure {
                errorMsg.value = it.message
            }
        }
    }

    /**
     * Método de limpieza para evitar que la UI procese eventos antiguos
     * al recrearse el fragmento (patrón SingleLiveEvent simplificado).
     */
    fun limpiarResultado() {
        ajustesResult.value = null
        ultimaAccion = ""
    }

}