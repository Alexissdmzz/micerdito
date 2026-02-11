package com.example.micerdito.data.preferencias

import android.content.Context

/**
 * @PreferenciasSesion es una clase que almacena los datos del usuario antes de cerrar la app,
 * asi permitiendo que no tenga que repetir todo el proceso siempre.
 */

class PreferenciasSesion(context: Context) {

    private val sharedPref = context.getSharedPreferences(
        "MiCerditoPrefs",
        Context.MODE_PRIVATE
    ) // Declaramos la variable

    // GUARDAMOS LOS DATOS DE SESIÓN DEL USUARIO YA LOGUEADO
    fun guardarSesion(id: String, nombre: String) {
        sharedPref.edit().apply {
            putString("userId", id)
            putString("nombre_usuario", nombre)
            putBoolean("isLogged", true)
            apply()
        }
    }

    fun estaLogueado(): Boolean = sharedPref.getBoolean("isLogged", false)

    fun getIdUsuario(): String = sharedPref.getString("userId", "") ?: ""

    fun getNombreUsuario(): String = sharedPref.getString("nombre_usuario", "Usuario") ?: "Usuario"

    fun setNombreUsuario(nombre: String) {
        sharedPref.edit().putString("nombre_usuario", nombre).apply()
    }

    fun limpiarSesion() {
        sharedPref.edit().clear().apply()
    }

    // --- AJUSTES VISUALES ---

    fun setModoOscuro(activado: Boolean) {
        sharedPref.edit().putBoolean("modo_oscuro", activado).apply()
    }

    fun esModoOscuro(): Boolean = sharedPref.getBoolean("modo_oscuro", false)

    fun setModoDaltonico(activado: Boolean) {
        sharedPref.edit().putBoolean("modo_daltonico", activado).apply()
    }

    fun esDaltonico(): Boolean = sharedPref.getBoolean("modo_daltonico", false)

}