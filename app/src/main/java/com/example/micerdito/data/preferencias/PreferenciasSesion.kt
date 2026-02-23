package com.example.micerdito.data.preferencias

import android.content.Context

/**
 * PREFERENCIAS - PreferenciasSesion:
 * Es una clase que almacena los datos del usuario antes de cerrar la app,
 * asi permitiendo que no tenga que repetir todo el proceso siempre.
 */

class PreferenciasSesion(context: Context) {

    private val sharedPref = context.getSharedPreferences(
        "MiCerditoPrefs",
        Context.MODE_PRIVATE
    ) // Declaramos la variable

    // Guardamos los datos de sesión del usuario ya logueado
    fun guardarSesion(id: String, nombre: String) {
        sharedPref.edit().apply {
            putString("userId", id)
            putString("nombre_usuario", nombre)
            putBoolean("isLogged", true)
            apply()
        }
    }

    // Booleano para verificar si el usuario esta logueado
    fun estaLogueado(): Boolean = sharedPref.getBoolean("isLogged", false)

    // Devuelve el id del usuario previamente almacenado
    fun getIdUsuario(): String = sharedPref.getString("userId", "") ?: ""

    // Delvuelve el nombre del usuario previamente almacenado
    fun getNombreUsuario(): String = sharedPref.getString("nombre_usuario", "Usuario") ?: "Usuario"

    // Inserta un nuevo nombre de usuario
    fun setNombreUsuario(nombre: String) {
        sharedPref.edit().putString("nombre_usuario", nombre).apply()
    }

    // Limpieza de las preferencias en caso de cerrar sesión
    fun limpiarSesion() {
        sharedPref.edit().clear().apply()
    }

    // --- AJUSTES VISUALES ---

    // Modo oscuro
    fun setModoOscuro(activado: Boolean) {
        sharedPref.edit().putBoolean("modo_oscuro", activado).apply()
    }

    // Booleano para verificar si el usuario puso el Modo Oscuro
    fun esModoOscuro(): Boolean = sharedPref.getBoolean("modo_oscuro", false)

    // Modo daltonico
    fun setModoDaltonico(activado: Boolean) {
        sharedPref.edit().putBoolean("modo_daltonico", activado).apply()
    }

    // Booleano para verificar si el usuario puso el Modo Daltonico
    fun esDaltonico(): Boolean = sharedPref.getBoolean("modo_daltonico", false)

}