package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * ACTIVITY - WelcomeActivity
 * Pantalla de decisión inicial.
 * Presenta al usuario las dos rutas principales para acceder a la aplicación:
 * iniciar sesión si ya tiene cuenta, o registrarse para crear un perfil nuevo.
 */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Vinculación de los botones personalizados de la interfaz
        val btnLogin = findViewById<View>(R.id.btnLoginWelcome)
        val btnCreateAccount = findViewById<View>(R.id.btnRegisterWelcome)

        setupListeners(btnLogin, btnCreateAccount)
    }

    /**
     * Configura los clics de los botones principales para dirigir el tráfico del usuario.
     */
    private fun setupListeners(btnLogin: View, btnCreateAccount: View) {

        // Navegación hacia la pantalla de inicio de sesión
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Navegación hacia el formulario para crear una nueva cuenta
        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}