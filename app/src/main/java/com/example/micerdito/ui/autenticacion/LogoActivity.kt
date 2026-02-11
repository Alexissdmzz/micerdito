package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * @LogoActivity es la clase donde mostramos la vista del xml de @activity_logo, usamos un Looper para que muestre la pantalla solo
 * durante 3 segundos.
 */

class LogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 3000)

    }

}