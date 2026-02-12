package com.example.micerdito.viewmodel.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.data.repositorio.AuthRepository
import kotlinx.coroutines.launch

/**
 * @AuthViewModel es la clase donde definimos toda la lógica de la pantalla de inicio de sesión o registro del usuario.
 */

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    val loginResult = MutableLiveData<LoginResponse?>()
    val registerResult = MutableLiveData<RegisterResponse?>()
    val errorMsg = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    private fun validarContraseña(pass: String): Boolean {
        val patronContraseña = """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?])(?=\S+$).{8,}$"""
        return pass.matches(patronContraseña.toRegex())
    }

    private fun validarCorreo(email: String): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun doLogin(email: String, pass: String) {
        // Validación previa en el ViewModel
        if (!validarCorreo(email)) {
            errorMsg.value = "Formato de correo inválido"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, pass)
            isLoading.value = false
            result.onSuccess { loginResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }

    fun doRegister(username: String, email: String, pass: String, repeatPass: String) {
        if (!validarCorreo(email)) {
            errorMsg.value = "El correo no es válido"
            return
        }

        if (!validarContraseña(pass)) {
            errorMsg.value = "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
            return
        }

        if (pass != repeatPass) {
            errorMsg.value = "Las contraseñas no coinciden"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(username, email, pass, repeatPass)
            isLoading.value = false
            result.onSuccess { registerResult.value = it }
            result.onFailure { errorMsg.value = it.message }
        }
    }
}