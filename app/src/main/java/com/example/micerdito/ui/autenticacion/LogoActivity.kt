package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * ACTIVITY - LogoActivity
 * Pantalla de presentación inicial.
 * Su objetivo es mostrar la identidad visual de la aplicación mientras
 * el sistema prepara la memoria, antes de navegar al flujo de bienvenida.
 */
class LogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        /**
         * TEMPORIZADOR DE TRANSICIÓN:
         * Se utiliza un manejador vinculado al hilo principal de la interfaz para programar
         * el cambio de pantalla tras 3 segundos de espera.
         */
        Handler(Looper.getMainLooper()).postDelayed({
            // Transición hacia la pantalla de bienvenida
            startActivity(Intent(this, WelcomeActivity::class.java))

            // Cierra esta pantalla para que el usuario no pueda volver a ver el logo
            // al pulsar el botón de ir atrás en su dispositivo.
            finish()
        }, 3000)
    }
}