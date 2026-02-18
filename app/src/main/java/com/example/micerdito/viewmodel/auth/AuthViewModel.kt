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
 * @AuthViewModel es la clase donde definimos toda la lógica de la pantalla de inicio de sesión o registro del usuario.
 */

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val sesionRepository = SesionRepository(application)

    val loginResult = MutableLiveData<LoginResponse?>()
    val registerResult = MutableLiveData<RegisterResponse?>()
    val forgotPasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val changePasswordResult = MutableLiveData<ForgotPasswordResponse?>()
    val errorMsg = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    fun estaLogueado(): Boolean = sesionRepository.estaLogueado()
    fun obtenerIdUsuario(): String = sesionRepository.getIdUsuario()
    fun obtenerNombreUsuario(): String = sesionRepository.getNombreUsuario()

    private fun validarContraseña(pass: String): Boolean {
        val patronContraseña = """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?])(?=\S+$).{8,}$"""
        return pass.matches(patronContraseña.toRegex())
    }

    private fun validarCorreo(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

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

    fun doRegister(username: String, email: String, pwd: String, repeatPwd: String, idPregunta: Int, respuesta: String) {
        if (!validarCorreo(email)) {
            errorMsg.value = "El correo no es válido"
            return
        }

        if (!validarContraseña(pwd)) {
            errorMsg.value = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
            return
        }

        if (pwd != repeatPwd) {
            errorMsg.value = "Las contraseñas no coinciden"
            return
        }

        isLoading.value = true
        viewModelScope.launch {

            val correoLimpio = email.lowercase().trim()

            val result = authRepository.register(username, email, pwd, repeatPwd, idPregunta, respuesta)

            isLoading.value = false
            result.onSuccess { registerResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }

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

    fun doChangePwd(email: String, respuesta: String, nuevaPwd: String) {
        if (!validarContraseña(nuevaPwd)) {
            errorMsg.value = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
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