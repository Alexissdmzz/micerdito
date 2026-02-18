package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.data.repositorio.HomeRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * @HomeViewModel centraliza la lógica de la pantalla principal.
 * Recupera los datos del repositorio usando el ID almacenado en la sesión.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeRepository() // Inicializamos el repositorio
    private val sesionRepository = SesionRepository(application)

    // LiveDatas para que la Activity observe el resultado
    val homeResult = MutableLiveData<HomeResponse>()
    val errorMsg = MutableLiveData<String>()
    val islimiteSuperado = MutableLiveData<Boolean>() // Booleano que usaremos en caso de que el dinero gastado supere el límite establecido

    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()
    fun esDaltonico(): Boolean = sesionRepository.esDaltonico()

    //LÓGICA DE LA CARGA DE LOS DATOS DEL USUARIO ALMACENADOS EN LA BBDD, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun cargarDatosDeUsuario() {

        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            errorMsg.value = "No se encontró el ID del usuario"
            return
        }

        viewModelScope.launch {
            val result = repository.obtenerDatosHome(idUsuario)

            result.onSuccess { data ->
                homeResult.value = data

                islimiteSuperado.value = data.total_dinerogastado > data.limite_mes
            }

            result.onFailure {
                errorMsg.value = it.message
            }
        }

    }

    fun cerrarSesion() {
        sesionRepository.cerrarSesion()
    }

}