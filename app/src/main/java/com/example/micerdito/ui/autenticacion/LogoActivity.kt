package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * ACTIVITY - LogoActivity
 * Esta clase gestiona la pantalla de presentación de la aplicación. Su objetivo es
 * reforzar la identidad visual de "Mi Cerdito" y servir como punto de entrada
 * antes de redirigir al flujo de bienvenida o autenticación.
 */

class LogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        /**
         * GESTIÓN DEL TIEMPO DE ESPERA (Delay):
         * Se utiliza un Handler vinculado al Looper principal (MainLooper) para programar
         * una acción tras un retardo de 3000 milisegundos (3 segundos).
         */
        Handler(Looper.getMainLooper()).postDelayed({
            // Definición del salto hacia la pantalla de bienvenida
            startActivity(Intent(this, WelcomeActivity::class.java))
            /**
             * Finalizamos esta actividad para que no quede en el stack de navegación.
             * Esto evita que el usuario regrese al Splash pulsando el botón "Atrás".
             */
            finish()
        }, 3000) // 3 segundos de exposición del logo

    }

}