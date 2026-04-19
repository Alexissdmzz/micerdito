package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.ui.home.HomeActivity
import com.example.micerdito.viewmodel.autenticacion.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * ACTIVITY - LoginActivity
 * Pantalla encargada de iniciar sesión y comprobar si el usuario ya estaba conectado.
 * Es la puerta de entrada principal a la aplicación.
 */
class LoginActivity : AppCompatActivity() {

    // Conexión con el ViewModel para gestionar los datos
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * AUTO-LOGIN:
         * Comprueba si hay una sesión guardada antes de cargar la pantalla.
         * Si el usuario ya está conectado, lo mandamos directamente al inicio.
         */
        if (viewModel.estaLogueado()) {
            irAHome(viewModel.obtenerIdUsuario(), viewModel.obtenerNombreUsuario())
            return
        }

        setContentView(R.layout.activity_login)

        // Vinculación de los elementos de la pantalla
        val tvBackLogin = findViewById<TextView>(R.id.tvBackLogin)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etPwd = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPwd = findViewById<TextView>(R.id.tvOlvido)

        setupObservers()
        setupListeners(btnLogin, etCorreo, etPwd, tvForgotPwd, tvBackLogin)
    }

    /**
     * Escucha las respuestas del servidor a través del ViewModel y actualiza la pantalla.
     */
    private fun setupObservers() {

        // Respuesta al intentar iniciar sesión
        viewModel.loginResult.observe(this) { response ->
            if (response?.success == true && response.user != null) {
                Toast.makeText(this, "Bienvenido: ${response.user.username}", Toast.LENGTH_SHORT)
                    .show()
                irAHome(response.user.id, response.user.username)
            } else {
                Toast.makeText(
                    this,
                    response?.message ?: "Error al iniciar sesión",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Respuesta en caso de que falle la conexión o el servidor
        viewModel.errorMsg.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Configura los clics en los botones y enlaces de la pantalla.
     */
    private fun setupListeners(
        btnLogin: Button,
        etCorreo: EditText,
        etPwd: EditText,
        tvForgotPwd: TextView,
        tvBackLogin: TextView
    ) {

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()

            // Comprobación rápida para no hacer esperar al servidor si faltan datos
            if (correo.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                viewModel.doLogin(correo, pwd)
            }
        }

        // Enlace para recuperar la cuenta
        tvForgotPwd.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Botón para volver a la pantalla de bienvenida
        tvBackLogin.setOnClickListener {
            finish() // Cierra la pantalla actual en lugar de crear una nueva
        }
    }

    /**
     * Manda al usuario a la pantalla principal y borra el Login del historial
     * para que no pueda volver atrás dándole al botón del móvil.
     */
    private fun irAHome(id: String, nombre: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("id_usuario", id)
            putExtra("nombre_usuario", nombre)
        }
        startActivity(intent)
        finish()
    }
}