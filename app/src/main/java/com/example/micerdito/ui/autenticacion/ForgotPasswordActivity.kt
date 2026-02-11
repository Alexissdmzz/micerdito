//package com.example.micerdito.ui.autenticacion
//
//import android.os.Bundle
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.micerdito.R
//import com.google.android.material.textfield.TextInputEditText
//
//class ForgotPasswordActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_forgot_password)
//
//        val etCorreo = findViewById<TextInputEditText>(R.id.etRecoverEmail)
//        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
//
//        btnEnviar.setOnClickListener {
//            val correo = etCorreo.text.toString().trim()
//
//            if (correo.isEmpty()) {
//                Toast.makeText(this, "Debes completar todos los campos", Toast.LENGTH_SHORT).show()
//            }
//
//            Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show()
//        }
//
//    }
//}