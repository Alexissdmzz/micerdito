package com.example.micerdito.ui.handlers

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class CameraHandler(private val context: Context) {

    var rutaFotoActual: String? = null
        private set

    /**
     * Crea un archivo temporal y devuelve su URI segura para la cámara.
     */
    fun generarUriParaCamara(): Uri? {
        val archivoFoto = try {
            crearArchivoFoto()
        } catch (ex: IOException) {
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

    private fun crearArchivoFoto(): File {
        val nombreFoto = "TICKET_${System.currentTimeMillis()}_"
        val directorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(nombreFoto, ".jpg", directorio).apply {
            rutaFotoActual = absolutePath
        }
    }

}