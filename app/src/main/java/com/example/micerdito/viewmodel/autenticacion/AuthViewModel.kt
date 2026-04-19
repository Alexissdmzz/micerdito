package com.example.micerdito.viewmodel.autenticacion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.data.repositorio.AuthRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - AuthViewModel
 * Centraliza la lógica de negocio para los flujos de inicio de sesión, registro
 * y recuperación de credenciales. Actúa como intermediario entre la capa de presentación
 * y los repositorios de datos, gestionando la concurrencia mediante corrutinas
 * para evitar el bloqueo del hilo principal de la interfaz de usuario.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los orígenes de datos: remoto (Auth) y local (Sesión)
    private val authRepository = AuthRepository()
    private val sesionRepository = SesionRepository(application)

    // Canales de comunicación reactiva (LiveData) observados por la vista
    val loginResult = MutableLiveData<LoginResponse?>()
    val registerResult = MutableLiveData<RegisterResponse?>()
    val forgotPasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val changePasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val errorMsg = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    /**
     * MÉTODOS DE CONSULTA DE SESIÓN:
     * Permiten a los controladores de vista conocer el estado de autenticación
     * sin acoplarse directamente a las preferencias locales del sistema.
     */
    fun estaLogueado(): Boolean = sesionRepository.estaLogueado()
    fun obtenerIdUsuario(): String = sesionRepository.getIdUsuario()
    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()

    /**
     * VALIDACIÓN DE COMPLEJIDAD DE CREDENCIALES:
     * Verifica mediante expresiones regulares que la contraseña cumpla con los estándares de seguridad:
     * Mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial.
     */
    private fun validarContraseña(pass: String): Boolean {
        val patronContraseña =
            """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?])(?=\S+$).{8,}$"""
        return pass.matches(patronContraseña.toRegex())
    }

    /**
     * VALIDACIÓN DE FORMATO DE CORREO:
     * Comprueba que la cadena introducida coincida con la estructura estándar
     * de una dirección de correo electrónico mediante patrones nativos de Android.
     */
    private fun validarCorreo(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * OPERACIÓN: Inicio de Sesión.
     * Gestiona la validación local, orquesta la petición de red y, en caso de éxito,
     * persiste los datos de identificación en el almacenamiento seguro del dispositivo.
     */
    fun doLogin(correo: String, pwd: String) {
        if (!validarCorreo(correo)) {
            errorMsg.value = "Formato de correo inválido"
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.login(correo, pwd)
            isLoading.value = false

            result.onSuccess { response ->
                if (response.success && response.user != null) {
                    sesionRepository.guardarSesion(
                        response.user.id.toString(),
                        response.user.username
                    )
                }
                loginResult.value = response
            }
            result.onFailure { errorMsg.value = it.message }
        }
    }

    /**
     * OPERACIÓN: Registro de Usuario.
     * Realiza verificaciones cruzadas de contraseñas y validación de formato
     * antes de emitir la carga útil hacia el servidor.
     */
    fun doRegister(
        username: String,
        email: String,
        pwd: String,
        repeatPwd: String,
        idPregunta: Int,
        respuesta: String
    ) {
        if (!validarCorreo(email)) {
            errorMsg.value = "El correo no es válido"
            return
        }

        if (!validarContraseña(pwd)) {
            errorMsg.value =
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
            return
        }

        if (pwd != repeatPwd) {
            errorMsg.value = "Las contraseñas no coinciden"
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            val result =
                authRepository.register(username, email, pwd, repeatPwd, idPregunta, respuesta)

            isLoading.value = false
            result.onSuccess { registerResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }

    /**
     * FLUJO DE RECUPERACIÓN (Fase 1):
     * Solicita el desafío de seguridad asociado a la identidad proporcionada.
     */
    fun fetchPregunta(email: String) {
        if (!validarCorreo(email)) {
            errorMsg.value = "El correo no es válido"
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.recuperarPregunta(email.lowercase().trim())
            isLoading.value = false

            result.onSuccess { forgotPasswordResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }

    /**
     * FLUJO DE RECUPERACIÓN (Fase 2):
     * Emite la resolución del desafío junto con la nueva credencial de acceso para su actualización.
     */
    fun doChangePwd(email: String, respuesta: String, nuevaPwd: String) {
        if (!validarContraseña(nuevaPwd)) {
            errorMsg.value =
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
            return
        }

        isLoading.value = true

        viewModelScope.launch {
            val result = authRepository.actualizarPwd(email.lowercase().trim(), respuesta, nuevaPwd)
            isLoading.value = false

            result.onSuccess { changePasswordResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }
}