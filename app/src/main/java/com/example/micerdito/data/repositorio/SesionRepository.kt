package com.example.micerdito.data.repositorio

import android.content.Context
import com.example.micerdito.data.preferencias.PreferenciasSesion

/**
 * PATRÓN REPOSITORIO - SesionRepository
 * Actúa como una Fachada (Facade Pattern) sobre la fuente de datos local (Local Data Source).
 * Aísla a la capa de presentación (ViewModels) de las dependencias nativas del framework
 * de Android (Context, SharedPreferences), garantizando que la lógica de negocio permanezca pura.
 */
class SesionRepository(context: Context) {

    // Instancia de la fuente de la verdad local
    private val preferenciasSesion = PreferenciasSesion(context)

    // ==========================================
    // ESTADO DE IDENTIDAD Y ACCESO
    // ==========================================

    /**
     * Consulta síncrona del estado de autenticación para resolución de enrutamiento (ej. Splash -> Home/Login).
     */
    fun estaLogueado(): Boolean = preferenciasSesion.estaLogueado()

    /**
     * Recupera el identificador único (UUID) en memoria caché para la firma de peticiones de red.
     */
    fun getIdUsuario(): String = preferenciasSesion.getIdUsuario()

    /**
     * Extrae el nombre de visualización (Display Name) cacheado para renderizado inmediato en UI.
     */
    fun getNombreUsuario(): String = preferenciasSesion.getNombreUsuario()

    /**
     * Persiste el token de sesión y metadatos tras una validación exitosa contra el servidor.
     * @param idUsuario UUID inmutable.
     * @param nombreUsuario Nombre de perfil.
     */
    fun guardarSesion(idUsuario: String, nombreUsuario: String) {
        preferenciasSesion.guardarSesion(idUsuario, nombreUsuario)
    }

    /**
     * Sincroniza la caché local tras una mutación de perfil exitosa en la base de datos remota.
     */
    fun actualizarNombre(nuevoNombreUsuario: String) {
        preferenciasSesion.setNombreUsuario(nuevoNombreUsuario)
    }

    /**
     * Invalida el estado de autenticación (Logout) y purga los datos locales por motivos de seguridad.
     */
    fun cerrarSesion() {
        preferenciasSesion.limpiarSesion()
    }

    // ==========================================
    // PREFERENCIAS DE ENTORNO
    // ==========================================

    /**
     * Consulta la tematización (Theme) preferida por el usuario.
     */
    fun esModoOscuro(): Boolean = preferenciasSesion.esModoOscuro()

    /**
     * Persiste un cambio de tematización para aplicarlo globalmente en la aplicación.
     */
    fun setModoOscuro(valor: Boolean) {
        preferenciasSesion.setModoOscuro(valor)
    }
}