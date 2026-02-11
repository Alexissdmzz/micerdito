package com.example.micerdito.ui.autenticacion

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * @RegisterActivity es la clase donde definimos los elementos interactivos del xml @activity_register para el usuario
 * en la pantalla de registro de usuario.
 */

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels() // Herramienta que nos deja conectar con la Lógica (ViewModel)
    private lateinit var btnRegistrarse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Mostramos la vista


        // INICIALIZAMOS LOS ELEMENTOS INTERACTIVOS
        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etCorreo = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPwd = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRepeatPwd = findViewById<TextInputEditText>(R.id.etRegRepeatPassword)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)

        // CONFIGURAR OBSERVADORES
        setupObservers()

        btnRegistrarse.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()
            val repeatPwd = etRepeatPwd.text.toString().trim()

            if (username.isEmpty() || correo.isEmpty() || pwd.isEmpty() || repeatPwd.isEmpty()) {
                Toast.makeText(this, "Tienes que rellenar todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Llamamos al ViewModel para procesar el registro
            viewModel.doRegister(username, correo, pwd, repeatPwd)
        }
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                    finish() // Regresa al Login tras el éxito
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // OBSERVAR MENSAJES DE ERROR
        viewModel.errorMsg.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        // OBSERVAR MENSAJES DE CARGA
        viewModel.isLoading.observe(this) { loading ->
            btnRegistrarse.isEnabled = !loading
        }
    }
}