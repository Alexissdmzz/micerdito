package com.example.micerdito.ui.autenticacion

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.micerdito.R
import com.example.micerdito.viewmodel.autenticacion.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * ACTIVITY - ForgotPasswordActivity
 * Se encarga de la pantalla para recuperar la contraseña. Funciona en dos pasos:
 * 1. Pide el correo para sacar la pregunta de seguridad de ese usuario.
 * 2. Comprueba la respuesta y guarda la contraseña nueva.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    // Conectamos con el ViewModel para que maneje la lógica por debajo
    private val viewModel: AuthViewModel by viewModels()

    // Nos sirve para saber en qué paso estamos (falso = paso 1, verdadero = paso 2)
    private var isStepTwo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Enganchamos las variables con los elementos visuales de la pantalla
        val tvBackForgotPwd = findViewById<TextView>(R.id.tvBackForgotPwd)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val tvPregunta = findViewById<TextView>(R.id.tvPreguntaSeguridad)
        val layoutNuevaPass = findViewById<LinearLayout>(R.id.layoutNuevaPass)
        val etRespuesta = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val etNuevaPass = findViewById<TextInputEditText>(R.id.etNuevaPassword)
        val btnVerificar = findViewById<Button>(R.id.btnVerificar)

        setupObservers(tvPregunta, layoutNuevaPass, btnVerificar)
        setupListeners(btnVerificar, etCorreo, etRespuesta, etNuevaPass, tvBackForgotPwd)
    }

    /**
     * Escucha lo que dice el ViewModel y actualiza la pantalla al momento.
     */
    private fun setupObservers(tvPregunta: TextView, layout: LinearLayout, btn: Button) {

        // Qué hacer cuando el servidor nos devuelve la pregunta secreta (Paso 1)
        viewModel.forgotPasswordResult.observe(this) { res ->
            if (res?.success == true) {
                // Mostramos la pregunta, sacamos los campos ocultos y cambiamos el botón
                tvPregunta.text = res.pregunta
                layout.visibility = View.VISIBLE
                btn.text = "RESTABLECER"
                isStepTwo = true
            } else {
                Toast.makeText(
                    this,
                    "No se ha podido verificar el correo. Inténtalo de nuevo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Qué hacer cuando el servidor confirma que se ha cambiado la contraseña (Paso 2)
        viewModel.changePasswordResult.observe(this) { res ->
            if (res?.success == true) {
                Toast.makeText(this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                finish() // Cerramos esta pantalla para volver al Login
            } else {
                Toast.makeText(
                    this,
                    "No se ha podido actualizar la contraseña. Inténtalo de nuevo",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // Por si falla internet o el servidor se cae
        viewModel.errorMsg.observe(this) { msg ->
            Toast.makeText(this, msg ?: "Error de conexión", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura qué pasa cuando el usuario pulsa los botones.
     */
    private fun setupListeners(
        btnVerificar: Button,
        etCorreo: EditText,
        etRespuesta: EditText,
        etNuevaPass: EditText,
        tvBackForgotPwd: TextView
    ) {

        btnVerificar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()

            if (!isStepTwo) {
                // PASO 1: Comprobamos el correo para pedir la pregunta secreta
                if (correo.isEmpty()) {
                    Toast.makeText(this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.fetchPregunta(correo)
                }
            } else {
                // PASO 2: Mandamos la respuesta y la contraseña nueva
                val respuesta = etRespuesta.text.toString().trim()
                val nuevaClave = etNuevaPass.text.toString().trim()

                if (respuesta.isEmpty() || nuevaClave.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Completa la respuesta y la nueva clave",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.doChangePwd(correo, respuesta, nuevaClave)
                }
            }
        }

        // Botón de atrás para cancelar y volver
        tvBackForgotPwd.setOnClickListener {
            finish()
        }
    }
}