package com.example.micerdito.viewmodel.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.home.CalendarioResponse
import com.example.micerdito.data.model.home.GastoResponse
import com.example.micerdito.data.repositorio.CalendarioRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - CalendarioViewModel:
 * Gestiona la lógica de la pantalla de Calendario. Obtiene y enseña todos
 * los movimientos realizados por el usuario filtrandolo por meses.
 */
class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios para separar la persistencia remota de la local
    private val repository = CalendarioRepository()
    private val sesionRepository = SesionRepository(application)

    // Datos del calendario (Puntos, fecha registro y gráfico)
    private val _calendarioData = MutableLiveData<CalendarioResponse?>()
    val calendarioData: LiveData<CalendarioResponse?> get() = _calendarioData

    // Datos para la lista de gastos diarios (RecyclerView)
    // Usamos GastoResponse porque es lo que devuelve obtenerGastosPorDia
    private val _gastosDelDia = MutableLiveData<GastoResponse?>()
    val gastosDelDia: LiveData<GastoResponse?> get() = _gastosDelDia

    // Estado de error para mostrar Toasts o mensajes en la UI
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Estado de carga (Loading)
    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    /**
     * Solicita al repositorio los datos necesarios para pintar el calendario y el gráfico.
     * @param mes Número del mes a consultar.
     * @param anio Año a consultar.
     * Utiliza el ID del usuario almacenado en la sesión local de forma automática.
     */
    fun obtenerDataCalendario(mes: Int, anio: Int) {
        val idUsuario = sesionRepository.getIdUsuario() ?: ""

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        _cargando.value = true

        viewModelScope.launch {
            // Ejecución de la llamada al repositorio dentro de la corrutina
            val result = repository.obtenerDatosCalendario(idUsuario, mes, anio)

            result.onSuccess { data ->
                // Encapsulamos la respuesta completa
                _calendarioData.value = data
                _cargando.value = false
            }.onFailure { e ->
                // Manejo de errores de red o servidor
                _error.value = "Error al cargar el calendario: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * Gestiona la petición de gastos detallados para un día específico seleccionado en el calendario.
     * Recupera el ID del usuario de la sesión activa y actualiza los estados de carga,
     * error y datos para que la interfaz reaccione en consecuencia.
     * @param anio Año del día seleccionado.
     * @param mes Mes del día seleccionado (1-12).
     * @param dia Día específico del mes.
     */
    fun obtenerGastosDia(anio: Int, mes: Int, dia: Int) {
        val idUsuario = sesionRepository.getIdUsuario() ?: ""

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        _cargando.value = true

        viewModelScope.launch {
            // Ejecución de la llamada al repositorio dentro de la corrutina
            val result = repository.obtenerGastosPorDia(idUsuario, anio, mes, dia)

            result.onSuccess { data ->
                // Encapsulamos la respuesta completa
                _gastosDelDia.value = data
                _cargando.value = false
            }.onFailure { e ->
                // Manejo de errores de red o servidor
                _error.value = "Error al cargar el calendario: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * Limpia el estado de error después de ser mostrado en la vista.
     */
    fun resetError() {
        _error.value = ""
    }
}