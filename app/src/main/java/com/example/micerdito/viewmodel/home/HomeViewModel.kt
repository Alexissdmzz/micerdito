package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Gasto
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.repositorio.HomeRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - HomeViewModel:
 * Motor lógico del Dashboard principal. Gestiona la recuperación de métricas financieras,
 * el historial de movimientos y la administración de preferencias de usuario como
 * el límite de gasto y el tema visual.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios para acceso a datos remotos (MySQL/PHP) y persistencia local (Prefs)
    private val repository = HomeRepository()
    private val sesionRepository = SesionRepository(application)

    // LiveData para la actualización reactiva de la interfaz de usuario
    val homeResult = MutableLiveData<HomeResponse>()
    val graficoResult = MutableLiveData<List<GastoPorCategoria>>()
    val movimientosResult = MutableLiveData<List<Gasto>>() // Lista de transacciones recientes
    val errorMsg = MutableLiveData<String>()

    /**
     * Lógica de Negocio: LiveData booleano que determina si el usuario ha excedido
     * su presupuesto mensual, permitiendo cambios de color dinámicos en la View.
     */
    val islimiteSuperado = MutableLiveData<Boolean>()

    // Consultas rápidas a la sesión local
    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()
    fun esDaltonico(): Boolean = sesionRepository.esDaltonico()

    /**
     * CARGA INTEGRAL DE DATOS:
     * Ejecuta dos peticiones de red simultáneas mediante corrutinas para poblar
     * el Dashboard de forma eficiente sin bloquear el hilo principal.
     */
    fun cargarDatosDeUsuario() {
        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            errorMsg.value = "No se encontró el ID del usuario"
            return
        }

        // Lanzamiento de corrutina en el ámbito del ViewModel
        viewModelScope.launch {
            // PETICIÓN 1: Datos globales (Totales, Mes y Límite)
            val resultHome = repository.obtenerDatosHome(idUsuario)
            resultHome.onSuccess { data ->
                homeResult.value = data
                // Lógica de validación: Comparamos gasto real vs presupuesto establecido
                islimiteSuperado.value = data.total_dinerogastado > data.limite_mes
            }.onFailure {
                errorMsg.value = "Error totales: ${it.message}"
            }

            // PETICIÓN 3: Datos para el gráfico circular
            val resultGrafico = repository.obtenerGastosPorCategoria(idUsuario)
            resultGrafico.onSuccess { lista ->
                // Enviamos la lista de categorías y totales al LiveData
                graficoResult.value = lista
            }.onFailure {
                errorMsg.value = "Error gráfico: ${it.message}"
            }

            // PETICIÓN 3: Historial de movimientos
            val resultMovs = repository.obtenerMovimientosRecientes(idUsuario)
            resultMovs.onSuccess { response ->
                movimientosResult.value = response.gastosRecientes
            }.onFailure {
                errorMsg.value = "Error movimientos: ${it.message}"
            }
        }
    }

    /**
     * ACTUALIZACIÓN DE PRESUPUESTO:
     * Persiste el nuevo límite en el servidor y fuerza una recarga de datos
     * para asegurar que el Dashboard refleje el cambio inmediatamente.
     */
    fun actualizarLimiteMensual(nuevoLimite: Double) {
        val id = sesionRepository.getIdUsuario()
        viewModelScope.launch {
            val result = repository.guardarLimite(id, nuevoLimite)
            result.onSuccess {
                // Sincronización: Refrescamos la UI tras el éxito en el servidor
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