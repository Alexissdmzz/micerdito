package com.example.micerdito.data.repositorio

import android.content.Context
import com.example.micerdito.data.preferencias.PreferenciasSesion

/**
 * REPOSITORIO - SesionRepository
 * Este repositorio gestiona el estado local de la aplicación. Actúa como un envoltorio (wrapper)
 * sobre SharedPreferences, permitiendo que el resto de la app acceda a datos de sesión
 * y configuración sin conocer los detalles de implementación del almacenamiento de Android.
 */

class SesionRepository(context: Context) {

    // Instancia de la clase auxiliar que maneja el archivo físico de SharedPreferences
    private val preferenciasSesion = PreferenciasSesion(context)

    /**
     * Verifica si existe una sesión activa en el dispositivo.
     */
    fun estaLogueado(): Boolean = preferenciasSesion.estaLogueado()

    /**
     * Recupera el UUID del usuario almacenado localmente.
     */
    fun getIdUsuario(): String = preferenciasSesion.getIdUsuario()

    /**
     * Obtiene el nombre del usuario para mostrarlo en la UI (Home/Ajustes).
     */
    fun getNombreUsuario(): String = preferenciasSesion.getNombreUsuario()

    /**
     * Persiste los datos básicos tras un login o registro exitoso.
     * @param idUsuario UUID proveniente del servidor.
     * @param nombreUsuario Nombre del perfil.
     */
    fun guardarSesion(idUsuario: String, nombreUsuario: String) {
        preferenciasSesion.guardarSesion(idUsuario, nombreUsuario)
    }

    /**
     * Sincroniza el nombre de usuario local tras una edición de perfil exitosa en el servidor.
     */
    fun actualizarNombre(nuevoNombreUsuario: String) {
        preferenciasSesion.setNombreUsuario(nuevoNombreUsuario)
    }

    /**
     * Elimina todos los datos de sesión (Logout), forzando el retorno a la pantalla de Login.
     */
    fun cerrarSesion() {
        preferenciasSesion.limpiarSesion()
    }

    /**
     * GESTIÓN DE CONFIGURACIÓN Y ACCESIBILIDAD:
     * Métodos para leer y escribir el estado del Modo Oscuro y Modo para Daltónicos.
     */
    fun esModoOscuro(): Boolean = preferenciasSesion.esModoOscuro()

    fun setModoOscuro(valor: Boolean) {
        preferenciasSesion.setModoOscuro(valor)
    }

    fun esDaltonico(): Boolean = preferenciasSesion.esDaltonico()

    fun setDaltonico(valor: Boolean) {
        preferenciasSesion.setModoDaltonico(valor)
    }

}