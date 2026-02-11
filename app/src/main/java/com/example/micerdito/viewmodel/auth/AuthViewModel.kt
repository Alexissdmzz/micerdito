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
    private val repository = AuthRepository() // Inicializamos el repositorio

    // LiveDatas para que la Activity observe el resultado
    val loginResult = MutableLiveData<LoginResponse?>()

    val registerResult = MutableLiveData<RegisterResponse?>()
    val errorMsg = MutableLiveData<String>() // Mensaje de error
    val isLoading = MutableLiveData<Boolean>() // Estado

    // FUNCIÓN DONDE VALIDAMOS EL FORMATO OBLIGATORIO PARA LA CONTRASEÑA
    private fun validarContraseña(pass: String): Boolean {
        // Usamos triple comilla para no volvernos locos con las barras inclinadas
        val patronContraseña =
            """^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!¿?])(?=\S+$).{8,}$"""
        return pass.matches(patronContraseña.toRegex())
    }

    // GUARDAMOS EL ID Y EL NOMBRE DEL USUARIO CUANDO SE LOGUEA
    fun guardarUsuario(preferenciasSesion: PreferenciasSesion, id: String, nombre: String) {
        preferenciasSesion.guardarSesion(id, nombre)
    }

    // VERIFICAMOS SI EL LOGIN FUE EXITOSO
    fun verificarSesion(preferenciasSesion: PreferenciasSesion): Boolean {
        return preferenciasSesion.estaLogueado()
    }

    //LÓGICA DEL LOGIN, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun doLogin(email: String, pass: String) {

        isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, pass)
            isLoading.value = false

            result.onSuccess {
                loginResult.value = it
            }.onFailure {
                errorMsg.value = it.message
            }
        }
    }

    //LÓGICA DEL REGISTRO, USAMOS CORRUTINAS PARA NO USAR EL HILO PRINCIPAL
    fun doRegister(username: String, email: String, pass: String, repeatPass: String) {

        // SI LA CONTRASEÑA NO TIENE EL FORMATO OBLIGATORIO LE LANZAMOS UN ERROR
        if (!validarContraseña(pass)) {
            errorMsg.value =
                "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número"
            return
        }

        // SI LA CONTRASEÑA NO ES IGUAL A LA CONTRASEÑA REPETIDA, LANZAMOS UN ERROR
        if (pass != repeatPass) {
            errorMsg.value = "Las contraseñas no coinciden"
            return
        }

        isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(username, email, pass, repeatPass)
            isLoading.value = false

            result.onSuccess {
                registerResult.value = it
            }.onFailure {
                errorMsg.value = it.message
            }
        }
    }
}