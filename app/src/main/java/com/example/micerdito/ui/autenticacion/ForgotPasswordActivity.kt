package com.example.micerdito.view.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordActivity : AppCompatActivity() {

    // Inicialización del ViewModel siguiendo el patrón delegado de Kotlin
    private val viewModel: AuthViewModel by viewModels()

    // Variable de control para saber si estamos en el paso 1 (correo) o paso 2 (respuesta/clave)
    private var isStepTwo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // 1. Referencias a los componentes del XML mediante ID
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val tvPregunta = findViewById<TextView>(R.id.tvPreguntaSeguridad)
        val layoutNuevaPass = findViewById<LinearLayout>(R.id.layoutNuevaPass)
        val etRespuesta = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val etNuevaPass = findViewById<TextInputEditText>(R.id.etNuevaPassword)
        val btnVerificar = findViewById<Button>(R.id.btnVerificar)

        // 2. Configuración de los Observadores de LiveData
        setupObservers(tvPregunta, layoutNuevaPass, btnVerificar)

        // 3. Lógica del botón principal
        btnVerificar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()

            if (!isStepTwo) {
                // FASE 1: Solicitar la pregunta vinculada al correo
                if (correo.isEmpty()) {
                    Toast.makeText(this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.fetchPregunta(correo)
                }
            } else {
                // FASE 2: Validar respuesta y actualizar a la nueva contraseña
                val respuesta = etRespuesta.text.toString().trim()
                val nuevaClave = etNuevaPass.text.toString().trim()

                if (respuesta.isEmpty() || nuevaClave.isEmpty()) {
                    Toast.makeText(this, "Completa la respuesta y la nueva clave", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.doChangePwd(correo, respuesta, nuevaClave)
                }
            }
        }
    }

    /**
     * Configura la escucha de cambios en el ViewModel para actualizar la interfaz.
     */
    private fun setupObservers(tvPregunta: TextView, layout: LinearLayout, btn: Button) {

        // Observa el resultado de la búsqueda del correo (Paso 1)
        viewModel.forgotPasswordResult.observe(this) { res ->
            if (res?.success == true) {
                // Actualizamos la UI para el segundo paso
                tvPregunta.text = res.pregunta
                layout.visibility = View.VISIBLE
                btn.text = "RESTABLECER"
                isStepTwo = true
            } else {
                Toast.makeText(this, res?.message ?: "Error al verificar correo", Toast.LENGTH_SHORT).show()
            }
        }

        // Observa el resultado del cambio de contraseña (Paso 2)
        viewModel.changePasswordResult.observe(this) { res ->
            if (res?.success == true) {
                Toast.makeText(this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                finish() // Cierra la actividad y regresa al Login
            } else {
                Toast.makeText(this, res?.message ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }

        // Observa errores generales (conexión, servidor, etc.)
        viewModel.errorMsg.observe(this) { msg ->
            Toast.makeText(this, "Error de red: $msg", Toast.LENGTH_SHORT).show()
        }

        // Opcional: Podrías observar viewModel.isLoading para mostrar un ProgressBar
    }
}