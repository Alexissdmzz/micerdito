package com.example.micerdito.ui.autenticacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.home.HomeActivity
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * @LoginActivity es la clase donde definimos los elementos interactivos del xml @activity_login para el usuario
 * en la pantalla del inicio de sesión.
 */

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var preferenciasSesion: PreferenciasSesion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenciasSesion = PreferenciasSesion(this)

        // COMPROBACIÓN DE SESIÓN (Ahora lo hace la Activity directamente)
        if (preferenciasSesion.estaLogueado()) {
            irAHome(preferenciasSesion.getIdUsuario(), preferenciasSesion.getNombreUsuario())
            return
        }

        setContentView(R.layout.activity_login)

        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etPwd = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        setupObservers()

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()

            // Delegamos la validación al ViewModel o la hacemos aquí para feedback rápido
            if (correo.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.doLogin(correo, pwd)
            }
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { response ->
            if (response != null) {
                if (response.success && response.user != null) {

                    // ACCIÓN DESDE LA VISTA: Guardamos los datos nosotros
                    preferenciasSesion.guardarSesion(
                        response.user.id,
                        response.user.username
                    )

                    Toast.makeText(this, "Bienvenido: ${response.user.username}", Toast.LENGTH_SHORT).show()
                    irAHome(response.user.id, response.user.username)
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorMsg.observe(this) { error ->
            // Si el error contiene "correo", podrías incluso marcar el EditText
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun irAHome(id: String, nombre: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("id_usuario", id)
            putExtra("nombre_usuario", nombre)
        }
        startActivity(intent)
        finish()
    }
}