package com.example.micerdito.data.preferencias

import android.content.Context

/**
 * LOCAL DATA SOURCE - PreferenciasSesion
 * Clase encargada de gestionar la persistencia de datos ligeros (clave-valor)
 * en el almacenamiento interno y cifrado del dispositivo.
 * Actúa como la "Fuente de la Verdad Local" para el estado de la sesión y los ajustes de UI.
 */
class PreferenciasSesion(context: Context) {

    // Bloque estático de constantes. Centraliza las claves de acceso para evitar
    // errores tipográficos y facilitar la mantenibilidad del código.
    companion object {
        private const val PREFS_NAME = "MiCerditoPrefs"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "nombre_usuario"
        private const val KEY_IS_LOGGED = "isLogged"
        private const val KEY_DARK_MODE = "modo_oscuro"
    }

    // Instancia privada de SharedPreferences en modo privado (aislado de otras apps)
    private val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ==========================================
    // GESTIÓN DE ESTADO DE SESIÓN
    // ==========================================

    /**
     * Persiste el token/ID y los metadatos del usuario tras una autenticación exitosa.
     * Utiliza apply() para una escritura asíncrona que no bloquea el Main Thread.
     */
    fun guardarSesion(id: String, nombre: String) {
        sharedPref.edit().apply {
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, nombre)
            putBoolean(KEY_IS_LOGGED, true)
            apply()
        }
    }

    // Verifica la persistencia de la sesión para evitar el flujo de login (Auto-Login)
    fun estaLogueado(): Boolean = sharedPref.getBoolean(KEY_IS_LOGGED, false)

    // Recupera el UUID del usuario. Devuelve un string vacío como medida de seguridad
    fun getIdUsuario(): String = sharedPref.getString(KEY_USER_ID, "") ?: ""

    // Recupera el nombre de visualización
    fun getNombreUsuario(): String = sharedPref.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"

    // Mutador granular para actualizar el nombre local si se edita el perfil en el servidor
    fun setNombreUsuario(nombre: String) {
        sharedPref.edit().putString(KEY_USER_NAME, nombre).apply()
    }

    // Purga completa del almacenamiento local
    fun limpiarSesion() {
        sharedPref.edit().clear().apply()
    }

    // ==========================================
    // AJUSTES DE INTERFAZ (UI/UX)
    // ==========================================

    // Persiste la preferencia de tematización del usuario
    fun setModoOscuro(activado: Boolean) {
        sharedPref.edit().putBoolean(KEY_DARK_MODE, activado).apply()
    }

    // Consulta el estado del tema para inyectarlo en la inicialización de la app
    fun esModoOscuro(): Boolean = sharedPref.getBoolean(KEY_DARK_MODE, false)
}