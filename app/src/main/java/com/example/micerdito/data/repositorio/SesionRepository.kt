package com.example.micerdito.data.repositorio

import android.content.Context
import com.example.micerdito.data.preferencias.PreferenciasSesion

/**
 * Repositorio encargado de manejar toda la información de sesión del usuario.
 *
 * Abstrae el uso de SharedPreferences para respetar la arquitectura MVVM.
 */

class SesionRepository(context: Context) {

    private val preferenciasSesion = PreferenciasSesion(context)

    fun estaLogueado(): Boolean = preferenciasSesion.estaLogueado()

    fun getIdUsuario(): String = preferenciasSesion.getIdUsuario()

    fun getNombreUsuario(): String = preferenciasSesion.getNombreUsuario()

    fun guardarSesion(idUsuario: String, nombreUsuario: String) {
        preferenciasSesion.guardarSesion(idUsuario, nombreUsuario)
    }

    fun actualizarNombre(nuevoNombreUsuario: String) {
        preferenciasSesion.setNombreUsuario(nuevoNombreUsuario)
    }

    fun cerrarSesion() {
        preferenciasSesion.limpiarSesion()
    }

    fun esModoOscuro(): Boolean = preferenciasSesion.esModoOscuro()

    fun setModoOscuro(valor: Boolean) {
        preferenciasSesion.setModoOscuro(valor)
    }

    fun esDaltonico(): Boolean = preferenciasSesion.esDaltonico()

    fun setDaltonico(valor: Boolean) {
        preferenciasSesion.setModoDaltonico(valor)
    }

}