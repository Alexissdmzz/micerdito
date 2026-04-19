package com.example.micerdito.ui.autenticacion

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.viewmodel.autenticacion.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * ACTIVITY - RegisterActivity
 * Pantalla encargada del formulario para crear nuevas cuentas.
 * Incluye la configuración de la pregunta de seguridad y comprueba
 * que los datos introducidos por el usuario sean correctos antes de enviarlos.
 */
class RegisterActivity : AppCompatActivity() {

    // Conexión con el ViewModel para gestionar los datos
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Vinculación de los elementos de la pantalla
        val tvBackRegister = findViewById<TextView>(R.id.tvBackRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etCorreo = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPwd = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRepeatPwd = findViewById<TextInputEditText>(R.id.etRegRepeatPassword)
        val etResp = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val spinner = findViewById<Spinner>(R.id.spinnerPreguntas)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        /**
         * CONFIGURACIÓN DEL DESPLEGABLE:
         * Carga la lista de preguntas de seguridad desde los archivos de texto de la app.
         * Utiliza un adaptador nativo de Android para mostrar las opciones.
         */
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.preguntas_seguridad,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        setupObservers()
        setupListeners(
            btnRegistrarse, etUsername, etCorreo, etPwd,
            etResp, etRepeatPwd, spinner, tvBackRegister, tvLogin
        )
    }

    /**
     * Escucha las respuestas del servidor a través del ViewModel y actualiza la pantalla.
     */
    private fun setupObservers() {

        // Respuesta al intentar registrar la cuenta
        viewModel.registerResult.observe(this) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                    finish() // Cierra el registro y vuelve a la pantalla anterior si hay éxito
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Respuesta en caso de que falle la conexión o el servidor
        viewModel.errorMsg.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        /**
         * GESTIÓN DEL ESTADO DE CARGA:
         * Desactiva el botón de registro mientras la petición está en curso
         * para evitar que se envíen datos duplicados si el usuario pulsa varias veces.
         */
        viewModel.isLoading.observe(this) { loading ->
            findViewById<Button>(R.id.btnRegistrarse).isEnabled = !loading
        }
    }

    /**
     * Configura los clics en los botones y las validaciones de texto.
     */
    private fun setupListeners(
        btnRegistrarse: Button,
        etUsername: EditText,
        etCorreo: EditText,
        etPwd: EditText,
        etResp: EditText,
        etRepeatPwd: EditText,
        spinner: Spinner,
        tvBackRegister: TextView,
        tvLogin: TextView
    ) {

        btnRegistrarse.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()
            val repeatPwd = etRepeatPwd.text.toString().trim()
            val resp = etResp.text.toString().trim()
            val idPregunta = spinner.selectedItemPosition

            // 1. Comprobación de campos vacíos
            if (username.isEmpty() || correo.isEmpty() || pwd.isEmpty() || resp.isEmpty()) {
                Toast.makeText(this, "Tienes que rellenar todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // 2. Comprobación de que las contraseñas sean idénticas
            if (pwd != repeatPwd) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Comprobación de que el usuario ha elegido una pregunta válida
            if (idPregunta == 0) {
                Toast.makeText(this, "Selecciona una pregunta de seguridad", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Si todo está correcto, iniciamos el registro
            viewModel.doRegister(username, correo, pwd, repeatPwd, idPregunta, resp)
        }

        // Botón para volver a la pantalla de bienvenida
        tvBackRegister.setOnClickListener {
            finish()
        }

        // Enlace para ir al inicio de sesión
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Cerramos el registro para no acumular pantallas
        }
    }
}