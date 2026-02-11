package com.example.micerdito.ui.autenticacion


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R

/**
 * @WelcomeActivity es la clase donde definimos los elementos interactivos del xml @activity_welcome para el usuario
 * en la pantalla de elección de acción.
 */

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome) // Mostramos la vista

        // INICIALIZAMOS LOS ELEMENTOS INTERACTIVOS
        val btnLogin = findViewById<View>(R.id.btnLoginWelcome)
        val btnCreateAccount = findViewById<View>(R.id.btnRegisterWelcome)

        // DEFINIMOS LAS ACCIONES AL CLICKAR EN LOS BOTONES
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}