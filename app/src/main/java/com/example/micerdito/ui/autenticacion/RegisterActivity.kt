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

/**
 * ACTIVITY - RegisterActivity
 * Gestiona el formulario de creación de nuevas cuentas.
 * Incluye la selección de preguntas de seguridad para la posterior recuperación
 * de la cuenta y validaciones de integridad de datos en el cliente.
 */
class RegisterActivity : AppCompatActivity() {

    // Instancia del ViewModel
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicialización de componentes de la vista
        val etUsername = findViewById<TextInputEditText>(R.id.etRegUsername)
        val etCorreo = findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPwd = findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRepeatPwd = findViewById<TextInputEditText>(R.id.etRegRepeatPassword)
        val etResp = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val spinner = findViewById<Spinner>(R.id.spinnerPreguntas)
        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)

        /**
         * CONFIGURACIÓN DEL SELECTOR (Spinner):
         * Carga el catálogo de preguntas de seguridad desde el recurso XML 'strings.xml'.
         * Se utiliza un ArrayAdapter para vincular los datos al componente visual.
         */
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.preguntas_seguridad, // Este nombre debe coincidir en strings.xml
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Activación de observadores LiveData
        setupObservers()

        /**
         * Lógica de envío del formulario.
         * Realiza validaciones críticas antes de invocar la API.
         */
        btnRegistrarse.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pwd = etPwd.text.toString().trim()
            val repeatPwd = etRepeatPwd.text.toString().trim()
            val resp = etResp.text.toString().trim()
            val idPregunta = spinner.selectedItemPosition

            // 1. Verificación de campos vacíos
            if (username.isEmpty() || correo.isEmpty() || pwd.isEmpty() || resp.isEmpty()) {
                Toast.makeText(this, "Tienes que rellenar todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Validación de coincidencia de contraseña
            if (pwd != repeatPwd) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Verificación de selección de pregunta
            if (idPregunta == 0) {
                Toast.makeText(this, "Selecciona una pregunta de seguridad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ejecución del registro a través del ViewModel
            viewModel.doRegister(username, correo, pwd, repeatPwd, idPregunta, resp)
        }
    }

    /**
     * Define la reacción de la UI ante los cambios de estado del ViewModel.
     */
    private fun setupObservers() {
        // Observa el resultado del registro
        viewModel.registerResult.observe(this) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()
                    finish() // Retorna al Login tras el éxito
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observa errores de red o excepciones técnicas
        viewModel.errorMsg.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        /**
         * Gestión del estado de carga (Loading):
         * Deshabilita el botón de registro mientras la petición está en curso
         * para evitar registros duplicados por clics múltiples.
         */
        viewModel.isLoading.observe(this) { loading ->
            findViewById<Button>(R.id.btnRegistrarse).isEnabled = !loading
        }
    }
}