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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * VIEWMODEL - CalendarioViewModel
 * Gestiona la lógica de negocio de la pantalla de Calendario. Coordina la obtención de
 * la actividad financiera agrupada por meses y el desglose diario de transacciones,
 * además de orquestar las operaciones de edición y borrado de registros.
 */
class CalendarioViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los orígenes de datos: remoto y local
    private val repository = CalendarioRepository()
    private val sesionRepository = SesionRepository(application)

    // Datos agregados del calendario (marcadores visuales, fecha de registro y gráfica)
    private val _calendarioData = MutableLiveData<CalendarioResponse?>()
    val calendarioData: LiveData<CalendarioResponse?> get() = _calendarioData

    // Colección de transacciones para un día específico
    private val _gastosDelDia = MutableLiveData<GastoResponse?>()
    val gastosDelDia: LiveData<GastoResponse?> get() = _gastosDelDia

    // Confirmación de estado tras operaciones de mutación (Edición/Borrado)
    private val _accionGastoResult = MutableLiveData<GastoResponse?>()
    val accionGastoResult: LiveData<GastoResponse?> get() = _accionGastoResult

    // Canal de propagación de errores hacia la capa de presentación
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Indicador de estado de carga asíncrona
    private val _cargando = MutableLiveData<Boolean>()

    /**
     * OPERACIÓN: Consulta mensual.
     * Solicita al repositorio el agregado de datos necesarios para pintar el calendario
     * interactivo y el gráfico circular de un mes específico.
     * @param mes Número del mes a consultar.
     * @param anio Año a consultar.
     */
    fun obtenerDataCalendario(mes: Int, anio: Int) {
        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        _cargando.value = true

        viewModelScope.launch {
            val result = repository.obtenerDatosCalendario(idUsuario, mes, anio)

            result.onSuccess { data ->
                _calendarioData.value = data
                _cargando.value = false
            }.onFailure { e ->
                _error.value = "Error al cargar el calendario: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * OPERACIÓN: Consulta diaria.
     * Gestiona la extracción del desglose de transacciones para un día concreto
     * seleccionado en la interfaz del calendario.
     * @param anio Año de la fecha seleccionada.
     * @param mes Mes de la fecha seleccionada.
     * @param dia Día exacto a consultar.
     */
    fun obtenerGastosDia(anio: Int, mes: Int, dia: Int) {
        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        _cargando.value = true

        viewModelScope.launch {
            val result = repository.obtenerGastosPorDia(idUsuario, anio, mes, dia)

            result.onSuccess { data ->
                _gastosDelDia.value = data
                _cargando.value = false
            }.onFailure { e ->
                _error.value = "Error al cargar los gastos del día: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * OPERACIÓN: Edición de transacción.
     * Orquesta la transformación de los datos primitivos a empaquetados MIME (Multipart)
     * para posibilitar la subida conjunta de texto y binarios multimedia hacia el servidor.
     */
    fun editarGasto(
        idGasto: String,
        titulo: String,
        importe: Double,
        descripcion: String,
        fotoActual: String,
        fotoPart: MultipartBody.Part?,
        fechaGasto: String
    ) {
        _cargando.value = true

        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        // Conversión de tipos de dominio a RequestBody estandarizado para red
        val idUsuarioBody = idUsuario.toRequestBody("text/plain".toMediaTypeOrNull())
        val idBody = idGasto.toRequestBody("text/plain".toMediaTypeOrNull())
        val tituloBody = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val importeBody = importe.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val fechaBody = fechaGasto.toRequestBody("text/plain".toMediaTypeOrNull())

        // Respaldo de la ruta preexistente para evitar orfandad de imágenes en servidor
        val fotoActualBody = fotoActual.toRequestBody("text/plain".toMediaTypeOrNull())

        viewModelScope.launch {
            val result = repository.editarGastos(
                idUsuarioBody,
                idBody,
                tituloBody,
                importeBody,
                descBody,
                fotoActualBody,
                fechaBody,
                fotoPart
            )

            result.onSuccess { data ->
                _accionGastoResult.value = data
                _cargando.value = false
            }.onFailure { e ->
                _error.value = "Error al editar el gasto: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * OPERACIÓN: Borrado de transacción.
     * Solicita la eliminación permanente del registro financiero en la base de datos remota.
     */
    fun eliminarGasto(idGasto: String) {
        _cargando.value = true

        val idUsuario = sesionRepository.getIdUsuario()

        if (idUsuario.isEmpty()) {
            _error.value = "Sesión no válida"
            return
        }

        viewModelScope.launch {
            val result = repository.deleteGastos(idUsuario, idGasto)

            result.onSuccess { data ->
                _accionGastoResult.value = data
                _cargando.value = false
            }.onFailure { e ->
                _error.value = "Error al eliminar el gasto: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * Mantenimiento de estado: Purga los mensajes de error tras ser consumidos por la vista.
     */
    fun resetError() {
        _error.value = ""
    }

    /**
     * Mantenimiento de estado: Limpia el resultado de la última acción (editar/borrar) para evitar
     * que la interfaz procese el evento de éxito múltiples veces (ej. al rotar la pantalla).
     */
    fun resetAccionGastoResult() {
        _accionGastoResult.value = null
    }
}