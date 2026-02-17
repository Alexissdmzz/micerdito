package com.example.micerdito.ui.autenticacion

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // INICIALIZAMOS LOS ELEMENTOS
        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etCorreo = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPwd = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRepeatPwd = findViewById<TextInputEditText>(R.id.etRegRepeatPassword)
        val etResp = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val spinner = findViewById<Spinner>(R.id.spinnerPreguntas)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        // --- CONFIGURACIÓN DEL SPINNER (LO QUE TE FALTABA) ---
        // Creamos el adaptador usando el array definido en strings.xml
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.preguntas_seguridad, // Este nombre debe coincidir en strings.xml
            android.R.layout.simple_spinner_item
        )
        // Diseño de la lista desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Asignamos el adaptador al Spinner
        spinner.adapter = adapter
        // ------------------------------------------------------

        setupObservers()

        btnRegistrarse.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()
            val repeatPwd = etRepeatPwd.text.toString().trim()
            val resp = etResp.text.toString().trim()
            val idPregunta = spinner.selectedItemPosition

            // Validaciones
            if (username.isEmpty() || correo.isEmpty() || pwd.isEmpty() || resp.isEmpty()) {
                Toast.makeText(this, "Tienes que rellenar todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pwd != repeatPwd) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validamos que no se haya quedado en la opción 0 ("Selecciona...")
            if (idPregunta == 0) {
                Toast.makeText(this, "Selecciona una pregunta de seguridad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.doRegister(username, correo, pwd, repeatPwd, idPregunta, resp)
        }
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorMsg.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { loading ->
            findViewById<Button>(R.id.btnRegistrarse).isEnabled = !loading
        }
    }
}