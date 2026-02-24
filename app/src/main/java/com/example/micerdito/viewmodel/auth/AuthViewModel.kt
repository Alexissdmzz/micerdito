package com.example.micerdito.viewmodel.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.data.repositorio.AuthRepository
import com.example.micerdito.data.repositorio.SesionRepository
import kotlinx.coroutines.launch

/**
 * VIEWMODEL - AuthViewModel:
 * Centraliza la lógica de negocio para Login, Registro y Recuperación de cuenta.
 * Actúa como intermediario entre la UI y los repositorios, gestionando hilos
 * mediante Corrutinas para no bloquear la interfaz de usuario.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorios: Uno para red (Auth) y otro para persistencia local (Sesion)
    private val authRepository = AuthRepository()
    private val sesionRepository = SesionRepository(application)

    // LiveData: Canales de comunicación reactiva con las Activities
    val loginResult = MutableLiveData<LoginResponse?>()
    val registerResult = MutableLiveData<RegisterResponse?>()
    val forgotPasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val changePasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val errorMsg = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    /**
     * MÉTODOS DE CONSULTA DE SESIÓN:
     * Permiten a las Activities conocer el estado del usuario sin acceder a las preferencias.
     */
    fun estaLogueado(): Boolean = sesionRepository.estaLogueado()
    fun obtenerIdUsuario(): String = sesionRepository.getIdUsuario()
    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()

    /**
     * VALIDACIÓN TÉCNICA (RegEx):
     * Verifica que la contraseña sea segura: 8+ caracteres, Mayúscula, Minúscula,
     * Número y Carácter Especial.
     */
    private fun validarContraseña(pass: String): Boolean {
        val patronContraseña =
            """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?])(?=\S+$).{8,}$"""
        return pass.matches(patronContraseña.toRegex())
    }

    /**
     * VALIDACIÓN DE CORREO:
     * Verifica que el correo sea seguro: @.
     */
    private fun validarCorreo(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * OPERACIÓN: Inicio de Sesión
     * Si el servidor confirma éxito, se persiste el ID y Nombre localmente
     * antes de avisar a la interfaz.
     */
    fun doLogin(correo: String, pwd: String) {
        if (!validarCorreo(correo)) {
            errorMsg.value = "Formato de correo inválido"
            return
        }

        isLoading.value = true
        // Lanzamiento de corrutina en el ámbito del ViewModel
        viewModelScope.launch {
            val result = authRepository.login(correo, pwd)
            isLoading.value = false
            result.onSuccess { response ->
                if (response.success && response.user != null) {
                    // PERSISTENCIA LOCAL AUTOMÁTICA
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
     * OPERACIÓN: Registro de Usuario
     * Incluye validación cruzada de contraseñas y limpieza de strings (trim/lowercase).
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
     * FLUJO DE RECUPERACIÓN: Fase 1 (Obtener pregunta)
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
            result.onSuccess {
                forgotPasswordResult.value = it
            }
            result.onFailure {
                errorMsg.value = it.message
            }
        }
    }

    /**
     * FLUJO DE RECUPERACIÓN: Fase 2 (Cambiar contraseña)
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
            result.onSuccess {
                changePasswordResult.value = it
            }
            result.onFailure {
                errorMsg.value = it.message
            }
        }
    }
}