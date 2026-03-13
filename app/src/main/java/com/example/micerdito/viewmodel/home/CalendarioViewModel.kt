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

    // Resultado de operaciones de escritura (Editar/Borrar un gasto)
    private val _accionGastoResult = MutableLiveData<GastoResponse?>()
    val accionGastoResult: LiveData<GastoResponse?> get() = _accionGastoResult

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
                _error.value = "Error al cargar los gastos del día: ${e.message}"
                _cargando.value = false
            }
        }
    }

    /**
     * Envía la solicitud al repositorio para actualizar los detalles de un gasto existente.
     * Realiza la conversión de datos a formato Multipart para permitir el envío de archivos.
     * * @param idGasto Identificador único (UUID) del gasto a editar.
     * @param titulo Nuevo nombre o concepto del gasto.
     * @param importe Valor numérico actualizado del gasto.
     * @param descripcion Nota detallada o aclaración adicional.
     * @param fotoPart Parte del formulario que contiene el archivo físico de la imagen (opcional).
     */
    fun editarGasto(
        idGasto: String,
        titulo: String,
        importe: Double,
        descripcion: String,
        fotoPart: MultipartBody.Part?
    ) {
        _cargando.value = true

        // Conversión de tipos primitivos a RequestBody para el envío Multipart
        val idBody = idGasto.toRequestBody("text/plain".toMediaTypeOrNull())
        val tituloBody = titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val importeBody = importe.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())

        viewModelScope.launch {
            // Ejecución de la llamada al repositorio con los datos convertidos
            val result = repository.editarGastos(
                idBody,
                tituloBody,
                importeBody,
                descBody,
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
     * Solicita al repositorio la eliminación permanente de un gasto mediante su identificador.
     * Emite el resultado en accionGastoResult para que la vista pueda confirmar el borrado.
     * @param idGasto Identificador único (UUID) del gasto a borrar.
     */
    fun eliminarGasto(idGasto: String) {
        _cargando.value = true

        viewModelScope.launch {
            // Ejecución de la llamada al repositorio dentro de la corrutina
            val result = repository.deleteGastos(idGasto)

            result.onSuccess { data ->
                // Encapsulamos la respuesta exitosa del servidor
                _accionGastoResult.value = data
                _cargando.value = false
            }.onFailure { e ->
                // Manejo de errores de red o servidor
                _error.value = "Error al eliminar el gasto: ${e.message}"
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

    /**
     * Limpia el resultado de la última acción (editar/borrar) para evitar
     * que los observadores se disparen repetidamente (ej. al rotar la pantalla).
     */
    fun resetAccionGastoResult() {
        _accionGastoResult.value = null
    }
}