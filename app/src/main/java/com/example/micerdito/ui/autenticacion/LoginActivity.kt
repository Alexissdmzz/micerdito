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

    private val viewModel: AuthViewModel by viewModels() // Herramienta que nos deja conectar con la Lógica (ViewModel)
    private lateinit var preferenciasSesion: PreferenciasSesion // Herramienta que guarda las credenciales del usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenciasSesion = PreferenciasSesion(this)

        // COMPROBACIÓN DE SESIÓN
        if (viewModel.verificarSesion(preferenciasSesion)) {
            irAHome(preferenciasSesion.getIdUsuario(), preferenciasSesion.getNombreUsuario())
            return
        }

        // SI NO HAY SESIÓN ACTIVA MUESTRA LA PANTALLA PARA LAS CREDENCIALES
        setContentView(R.layout.activity_login)


        // INICIALIZAMOS LOS ELEMENTOS INTERACTIVOS
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val etPwd = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // CONFIGURAR OBSERVADORES
        setupObservers()

        // EVENTO CLICK BOTÓN LOGIN
        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()

            if (correo.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.doLogin(correo, pwd)
            }
        }
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { response ->
            if (response != null) {
                if (response.success && response.user != null) {
                    // GUARDAR SESIÓN
                    viewModel.guardarUsuario(
                        preferenciasSesion,
                        response.user.id,
                        response.user.username
                    )

                    Toast.makeText(
                        this,
                        "Bienvenido: ${response.user.username}",
                        Toast.LENGTH_SHORT
                    ).show()
                    irAHome(response.user.id, response.user.username)
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorMsg.observe(this) { error ->
            Toast.makeText(this, "Error de conexión: $error", Toast.LENGTH_LONG).show()
        }
    }

    // SI NO HAY ERRORES, REDIRIGE A LA PANTALLA HOME
    private fun irAHome(id: String, nombre: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("id_usuario", id)
            putExtra("nombre_usuario", nombre)
        }
        startActivity(intent)
        finish()
    }
}