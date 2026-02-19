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
 * @AjustesViewModel es la clase donde definimos toda la lógica de la pantalla de ajustes.
 */
class AjustesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AjustesRepository() // Inicializamos el repositorio
    private val sesionRepository = SesionRepository(application)

    // LiveDatas para que la Activity observe el resultado
    val ajustesResult = MutableLiveData<AjustesResponse?>()
    val errorMsg = MutableLiveData<String>()
    var ultimaAccion: String = "" // Declaramos la ultima acción para no solapar acciones
    var nombreTemporal: String = "" // Usamos esta variable para almacenar el nombre de la BBDD si se llega a cambiar

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


    //LÓGICA DEL BORRADO DE LA CUENTA, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun borrarCuenta() {
        ultimaAccion = "BORRAR"
        val idUsuario = sesionRepository.getIdUsuario()

        viewModelScope.launch {
            val result = repository.eliminarUsuario(idUsuario)

            result.onSuccess {
                ajustesResult.value = it
            }.onFailure {
                errorMsg.value = it.message
            }
        }

    }

    //LÓGICA DE EL CAMBIO DEL NOMBRE DE USUARIO DE LA CUENTA, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun editarUsuario(nuevoNombre: String) {
        ultimaAccion = "EDITAR"
        nombreTemporal = nuevoNombre
        val idUsuario = sesionRepository.getIdUsuario()

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

    // LIMPIAMOS VARIABLES PARA QUE NO SE MEZCLEN
    fun limpiarResultado() {
        ajustesResult.value = null
        ultimaAccion = ""
    }

}