package com.example.micerdito.ui.handlers

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * HANDLER - CameraHandler:
 * Centraliza la lógica de captura fotográfica, gestionando la creación de archivos
 * temporales y la generación de URIs seguras para la interacción con la cámara del sistema.
 */
class CameraHandler(private val context: Context) {

    // Almacena la ubicación física del último archivo generado para su posterior procesamiento
    var rutaFotoActual: String? = null
        private set

    /**
     * GESTIÓN DE URI SEGURA:
     * Crea un archivo físico y genera una URI de contenido mediante FileProvider.
     * Esto evita exponer rutas directas del sistema de archivos, cumpliendo con las políticas de Android.
     */
    fun generarUriParaCamara(): Uri? {
        val archivoFoto = try {
            crearArchivoFoto()
        } catch (ex: IOException) {
            null
        }

        return archivoFoto?.let {
            // Vinculación con el FileProvider definido en el AndroidManifest
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
        }
    }

    /**
     * PERSISTENCIA TEMPORAL:
     * Genera un archivo .jpg en el directorio privado de imágenes de la App.
     * Usa un prefijo con marca de tiempo (timestamp) para garantizar nombres únicos.
     */
    private fun crearArchivoFoto(): File {
        val nombreFoto = "TICKET_${System.currentTimeMillis()}_"
        val directorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(nombreFoto, ".jpg", directorio).apply {
            // Guardamos la ruta absoluta para recuperar la imagen tras la captura
            rutaFotoActual = absolutePath
        }
    }
}