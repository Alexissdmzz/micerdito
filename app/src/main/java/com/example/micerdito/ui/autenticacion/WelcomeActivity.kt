package com.example.micerdito.ui.autenticacion


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * ACTIVITY - WelcomeActivity:
 * Esta clase sirve como el punto de decisión inicial para el usuario.
 * Presenta una interfaz simplificada con las dos rutas principales de acceso
 * al sistema: Autenticación (Login) o Creación de perfil (Registro).
 */

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome) // Mostramos la vista

        /**
         * INICIALIZACIÓN DE COMPONENTES:
         * Se utilizan vistas genéricas (View) para capturar los eventos de clic
         * de los botones personalizados en el XML.
         */
        val btnLogin = findViewById<View>(R.id.btnLoginWelcome)
        val btnCreateAccount = findViewById<View>(R.id.btnRegisterWelcome)

        setupListeners(btnLogin, btnCreateAccount)
    }

    private fun setupListeners(btnLogin: View, btnCreateAccount: View) {
        /**
         * GESTIÓN DE NAVEGACIÓN - LOGIN:
         * Redirige al usuario hacia la pantalla de inicio de sesión.
         */
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        /**
         * GESTIÓN DE NAVEGACIÓN - REGISTRO:
         * Redirige al usuario hacia el formulario de alta de nueva cuenta.
         */
        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}