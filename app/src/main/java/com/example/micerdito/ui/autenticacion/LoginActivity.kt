package com.example.micerdito.ui.autenticacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.home.HomeActivity
import com.example.micerdito.view.auth.ForgotPasswordActivity
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * ACTIVITY - LoginActivity
 * Gestiona la autenticación de usuarios y la verificación de sesiones activas.
 * Actúa como el controlador principal de la primera pantalla de la aplicación.
 */

class LoginActivity : AppCompatActivity() {

    // Inicialización del ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * COMPROBACIÓN DE SESIÓN (Auto-Login):
         * Antes de inflar la vista del login, consultamos al ViewModel si existen credenciales
         * guardadas en SharedPreferences. Si es así, saltamos directamente al Home.
         */
        if (viewModel.estaLogueado()) {
            irAHome(viewModel.obtenerIdUsuario(), viewModel.obtenerNombreUsuario())
            return
        }

        setContentView(R.layout.activity_login)

        // Inicialización de componentes de la vista
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etPwd = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPwd = findViewById<TextView>(R.id.tvOlvido)

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers()

        // Configuración de interraciones
        setupListeners(btnLogin, etCorreo, etPwd, tvForgotPwd)
    }

    /**
     * Define los observadores que reaccionarán a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        // Observa el resultado del intento de inicio de sesión
        viewModel.loginResult.observe(this) { response ->
            if (response?.success == true && response.user != null) {

                Toast.makeText(this, "Bienvenido: ${response.user.username}", Toast.LENGTH_SHORT)
                    .show()
                // Navegación al Dashboard principal tras éxito
                irAHome(response.user.id, response.user.username)
            } else {
                // Muestra el mensaje de error proveniente del servidor
                Toast.makeText(this, response?.message ?: "Error de login", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Observa errores críticos de red o excepciones del servidor
        viewModel.errorMsg.observe(this) { error ->
            // Si el error contiene "correo", podrías incluso marcar el EditText
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Configuración de interacciones del usuario.
     */
    private fun setupListeners(
        btnLogin: Button,
        etCorreo: EditText,
        etPwd: EditText,
        tvForgotPwd: TextView
    ) {
        /**
         * Acción del botón de acceso.
         * Realiza una validación previa en el cliente para ahorrar peticiones innecesarias al servidor.
         */
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()

            // Delegamos la validación al ViewModel o la hacemos aquí para feedback rápido
            if (correo.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Inicia el proceso de autenticación asíncrona
                viewModel.doLogin(correo, pwd)
            }
        }

        // Navegación hacia el flujo de recuperación de contraseña
        tvForgotPwd.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    /**
     * Gestiona la transición hacia la HomeActivity, pasando los datos de sesión
     * necesarios y finalizando la actividad actual para evitar que el usuario regrese
     * al login pulsando el botón "Atrás".
     */
    private fun irAHome(id: String, nombre: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("id_usuario", id)
            putExtra("nombre_usuario", nombre)
        }
        startActivity(intent)
        finish() // Elimina esta Activity del stack de navegación
    }
}