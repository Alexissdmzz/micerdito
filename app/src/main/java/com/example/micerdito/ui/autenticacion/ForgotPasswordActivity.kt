package com.example.micerdito.ui.autenticacion

import android.content.Intent
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
import com.example.micerdito.viewmodel.auth.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

/**
 * ACTIVITY - ForgotPasswordActivity
 * Gestiona el flujo de recuperación de cuenta en dos fases:
 * 1. Identificación del usuario y obtención de su pregunta de seguridad.
 * 2. Verificación de la respuesta y actualización de la credencial (password).
 */
class ForgotPasswordActivity : AppCompatActivity() {

    // Inicialización del ViewModel
    private val viewModel: AuthViewModel by viewModels()

    // Variable de control para saber si estamos en el paso 1 (correo) o paso 2 (respuesta/clave)
    private var isStepTwo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inicialización de componentes de la vista
        val tvBackForgotPwd = findViewById<TextView>(R.id.tvBackForgotPwd)
        val etCorreo = findViewById<TextInputEditText>(R.id.etCorreo)
        val tvPregunta = findViewById<TextView>(R.id.tvPreguntaSeguridad)
        val layoutNuevaPass = findViewById<LinearLayout>(R.id.layoutNuevaPass)
        val etRespuesta = findViewById<TextInputEditText>(R.id.etRespuestaSeguridad)
        val etNuevaPass = findViewById<TextInputEditText>(R.id.etNuevaPassword)
        val btnVerificar = findViewById<Button>(R.id.btnVerificar)

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers(tvPregunta, layoutNuevaPass, btnVerificar)

        // Configuración de interraciones
        setupListeners(btnVerificar, etCorreo, etRespuesta, etNuevaPass, tvBackForgotPwd)
    }

    /**
     * Suscripción a los LiveData del ViewModel.
     * Implementa el patrón Observer para mantener la UI sincronizada con los datos.
     */
    private fun setupObservers(tvPregunta: TextView, layout: LinearLayout, btn: Button) {

        // Observador para la fase 1: Recuperación de pregunta
        viewModel.forgotPasswordResult.observe(this) { res ->
            if (res?.success == true) {
                // Transición visual: Mostramos campos ocultos y cambiamos texto del botón
                tvPregunta.text = res.pregunta
                layout.visibility = View.VISIBLE
                btn.text = "RESTABLECER"
                isStepTwo = true
            } else {
                Toast.makeText(
                    this,
                    res?.message ?: "Error al verificar correo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observador para la fase 2: Resultado de la actualización
        viewModel.changePasswordResult.observe(this) { res ->
            if (res?.success == true) {
                Toast.makeText(this, "¡Contraseña actualizada con éxito!", Toast.LENGTH_LONG).show()
                finish() // Finaliza la actividad y retorna al LoginActivity
            } else {
                Toast.makeText(this, res?.message ?: "Error al actualizar", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Observador de errores globales de red o servidor
        viewModel.errorMsg.observe(this) { msg ->
            Toast.makeText(this, "Error de red: $msg", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configuración de interacciones del usuario.
     */
    private fun setupListeners(
        btnVerificar: Button,
        etCorreo: EditText,
        etRespuesta: EditText,
        etNuevaPass: EditText,
        tvBackForgotPwd: TextView
    ) {
        /**
         * Lógica del botón de acción principal.
         * Su comportamiento varía dinámicamente según el estado de 'isStepTwo'.
         */
        btnVerificar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()

            if (!isStepTwo) {
                // FASE 1: Validación de identidad
                if (correo.isEmpty()) {
                    Toast.makeText(this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Petición asíncrona para recuperar la pregunta de seguridad
                    viewModel.fetchPregunta(correo)
                }
            } else {
                // FASE 2: Verificación de respuesta y cambio de pwd
                val respuesta = etRespuesta.text.toString().trim()
                val nuevaClave = etNuevaPass.text.toString().trim()

                if (respuesta.isEmpty() || nuevaClave.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Completa la respuesta y la nueva clave",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Petición asíncrona para actualizar la contraseña en BD
                    viewModel.doChangePwd(correo, respuesta, nuevaClave)
                }
            }
        }

        // Volver a la pantalla anterior
        tvBackForgotPwd.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}