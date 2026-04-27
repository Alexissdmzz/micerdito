package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.Gasto
import com.example.micerdito.data.model.home.GastoPorCategoria
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.repositorio.HomeRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - HomeViewModel
 * Motor lógico del panel de control principal. Gestiona la recuperación asíncrona de
 * métricas financieras, el historial de movimientos y la administración de preferencias
 * de usuario, como el límite de gasto y el tema visual.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los repositorios para acceso a datos remotos y persistencia local
    private val repository = HomeRepository()
    private val sesionRepository = SesionRepository(application)

    // ==========================================
    // CANALES DE ESTADO REACTIVO (Backing Properties)
    // ==========================================

    private val _homeResult = MutableLiveData<HomeResponse>()
    val homeResult: LiveData<HomeResponse> get() = _homeResult

    private val _graficoResult = MutableLiveData<List<GastoPorCategoria>>()
    val graficoResult: LiveData<List<GastoPorCategoria>> get() = _graficoResult

    private val _movimientosResult = MutableLiveData<List<Gasto>>()
    val movimientosResult: LiveData<List<Gasto>> get() = _movimientosResult

    private val _errorMsg = MutableLiveData<String>()

    /**
     * Lógica de Negocio: Determina si el usuario ha excedido su presupuesto mensual,
     * permitiendo a la capa de presentación aplicar alertas visuales semánticas.
     */
    private val _islimiteSuperado = MutableLiveData<Boolean>()
    val islimiteSuperado: LiveData<Boolean> get() = _islimiteSuperado

    // ==========================================
    // CONSULTAS DE SESIÓN LOCAL
    // ==========================================

    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()
    fun esModoOscuro(): Boolean = sesionRepository.esModoOscuro()

    // ==========================================
    // OPERACIONES DE RED
    // ==========================================

    /**
     * CARGA INTEGRAL DE DATOS:
     * Orquesta tres peticiones de red concurrentes mediante corrutinas para poblar
     * la vista principal de forma eficiente sin bloquear el hilo de la interfaz.
     */
    fun cargarDatosDeUsuario() {
        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            _errorMsg.value = "No se encontró el identificador de usuario"
            return
        }

        viewModelScope.launch {
            // PETICIÓN 1: Datos globales (Totales, Mes y Límite)
            val resultHome = repository.obtenerDatosHome(idUsuario)
            resultHome.onSuccess { data ->
                _homeResult.value = data
                // Evaluación de reglas de negocio: Gasto real frente a presupuesto establecido
                _islimiteSuperado.value = data.totalDineroGastado > data.limiteMes
            }.onFailure {
                _errorMsg.value = "Error en totales: ${it.message}"
            }

            // PETICIÓN 2: Desglose analítico para la gráfica circular
            val resultGrafico = repository.obtenerGastosPorCategoria(idUsuario)
            resultGrafico.onSuccess { lista ->
                _graficoResult.value = lista
            }.onFailure {
                _errorMsg.value = "Error en gráfica: ${it.message}"
            }

            // PETICIÓN 3: Historial reciente de transacciones
            val resultMovs = repository.obtenerMovimientosRecientes(idUsuario)
            resultMovs.onSuccess { response ->
                _movimientosResult.value = response.gastosRecientes
            }.onFailure {
                _errorMsg.value = "Error en movimientos: ${it.message}"
            }
        }
    }

    /**
     * ACTUALIZACIÓN DE PRESUPUESTO:
     * Persiste el nuevo límite de gasto en la base de datos remota y fuerza una recarga
     * del estado global para asegurar que la interfaz refleje el cambio inmediatamente.
     */
    fun actualizarLimiteMensual(nuevoLimite: Double) {
        val id = sesionRepository.getIdUsuario()

        viewModelScope.launch {
            val result = repository.guardarLimite(id, nuevoLimite)

            result.onSuccess {
                // Sincronización: Refrescamos la UI tras confirmar el guardado en el servidor
                cargarDatosDeUsuario()
            }.onFailure {
                _errorMsg.value = "No se pudo actualizar el límite establecido"
            }
        }
    }
}