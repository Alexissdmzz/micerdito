package com.example.micerdito.ui.handlers

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * GESTOR - CameraHandler
 * Componente encargado de aislar la lógica de captura fotográfica.
 * Administra la creación de archivos temporales y la generación de URIs seguras
 * para interactuar con la cámara del sistema operativo.
 */
class CameraHandler(private val context: Context) {

    /**
     * Almacena la ruta absoluta del último archivo generado en el almacenamiento local
     * para su posterior procesamiento o envío al servidor.
     */
    var rutaFotoActual: String? = null
        private set

    /**
     * Crea un archivo físico y genera un identificador seguro mediante FileProvider.
     * Esta abstracción evita exponer rutas directas del sistema de archivos,
     * cumpliendo estrictamente con las políticas de seguridad de Android.
     */
    fun generarUriParaCamara(): Uri? {
        val archivoFoto = try {
            crearArchivoFoto()
        } catch (e: IOException) {
            android.util.Log.e("CameraHandler", "Error al crear el archivo de imagen", e)
            null
        }

        return archivoFoto?.let {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
        }
    }

    /**
     * Instancia un archivo de imagen en el directorio privado de la aplicación.
     * Utiliza una marca de tiempo del sistema operativo para garantizar
     * la unicidad del documento y evitar colisiones de sobreescritura.
     */
    private fun crearArchivoFoto(): File {
        val nombreFoto = "TICKET_${System.currentTimeMillis()}_"
        val directorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(nombreFoto, ".jpg", directorio).apply {
            rutaFotoActual = absolutePath
        }
    }
}