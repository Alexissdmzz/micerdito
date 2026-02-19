package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Gasto
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.repositorio.HomeRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeRepository()
    private val sesionRepository = SesionRepository(application)

    val homeResult = MutableLiveData<HomeResponse>()
    // NUEVO: LiveData específico para la lista de movimientos
    val movimientosResult = MutableLiveData<List<Gasto>>()

    val errorMsg = MutableLiveData<String>()
    val islimiteSuperado = MutableLiveData<Boolean>()

    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()
    fun esDaltonico(): Boolean = sesionRepository.esDaltonico()

    fun cargarDatosDeUsuario() {
        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            errorMsg.value = "No se encontró el ID del usuario"
            return
        }

        viewModelScope.launch {
            // PETICIÓN 1: Totales, Mes y Límite
            val resultHome = repository.obtenerDatosHome(idUsuario)
            resultHome.onSuccess { data ->
                homeResult.value = data
                islimiteSuperado.value = data.total_dinerogastado > data.limite_mes
            }.onFailure {
                errorMsg.value = "Error totales: ${it.message}"
            }

            // PETICIÓN 2: Últimos movimientos (Llamada al nuevo PHP/SP)
            val resultMovs = repository.obtenerMovimientosRecientes(idUsuario)
            resultMovs.onSuccess { response ->
                // Suponiendo que tu MovimientosResponse tiene la lista en 'gastos_recientes'
                movimientosResult.value = response.gastosRecientes
            }.onFailure {
                errorMsg.value = "Error movimientos: ${it.message}"
            }
        }
    }

    fun actualizarLimiteMensual(nuevoLimite: Double) {
        val id = sesionRepository.getIdUsuario()
        viewModelScope.launch {
            val result = repository.guardarLimite(id, nuevoLimite)
            result.onSuccess {
                // Una vez guardado con éxito, refrescamos los datos para que el
                // gráfico y los textos se actualicen con el nuevo límite
                cargarDatosDeUsuario()
            }
            result.onFailure {
                errorMsg.value = "No se pudo actualizar el límite"
            }
        }
    }

    fun cerrarSesion() {
        sesionRepository.cerrarSesion()
    }
}