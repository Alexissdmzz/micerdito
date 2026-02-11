package com.example.micerdito.viewmodel.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.data.repositorio.HomeRepository
import kotlinx.coroutines.launch

/**
 * @HomeViewModel es la clase donde definimos toda la lógica de la pantalla de Home.
 */
class HomeViewModel : ViewModel() {

    private val repository = HomeRepository() // Inicializamos el repositorio

    // LiveDatas para que la Activity observe el resultado
    val homeResult = MutableLiveData<HomeResponse>()
    val errorMsg = MutableLiveData<String>() // Mensaje de error
    val islimiteSuperado =
        MutableLiveData<Boolean>() // Booleano que usaremos en caso de que el dinero gastado supere el límite establecido

    //LÓGICA DE LA CARGA DE LOS DATOS DEL USUARIO ALMACENADOS EN LA BBDD, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun cargarDatosDeUsuario(preferenciasSesion: PreferenciasSesion) {

        val idUsuario = preferenciasSesion.getIdUsuario()

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

}