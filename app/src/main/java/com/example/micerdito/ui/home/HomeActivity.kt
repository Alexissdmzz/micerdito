package com.example.micerdito.ui.home

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.micerdito.R
import com.example.micerdito.ui.fragments.AjustesFragment
import com.example.micerdito.ui.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.OnBackPressedCallback
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.fragments.GastosCompartidosFragment

/**
 * @HomeActivity es la clase donde definimos el header y el footer del xml @activity_home para el usuario
 * en la pantalla del Home.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var preferenciasSesion: PreferenciasSesion // Declaramos las preferencias
    private var Salir = false

    override fun onCreate(savedInstanceState: Bundle?) {
        preferenciasSesion = PreferenciasSesion(this) // Inicializamos las preferencias

        // SI ES DALTONICO CAMBIAMOS EL TEMA DE LA APP
        if (preferenciasSesion.esDaltonico()) {
            setTheme(R.style.Theme_MiCerdito_Daltonico)
        } else {
            setTheme(R.style.Theme_MiCerdito)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Mostramos la vista


        // INICIALIZAMOS LOS ELEMENTOS INTERACTIVOS
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        configurarBotonSalir()

        tvWelcome.text = "Hola, ${preferenciasSesion.getNombreUsuario()}" // Mensaje del Header

        // Cargar Home por defecto al abrir
        if (savedInstanceState == null) {
            cargarFragmento(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Navegación por clics en el Footer
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    tvWelcome.visibility = android.view.View.VISIBLE
                    cargarFragmento(HomeFragment())
                }
                //R.id.nav_calendario
                R.id.nav_anadir_gasto -> {
                    tvWelcome.visibility = android.view.View.GONE
                    //cargarFragmento()
                }
                R.id.nav_gastos_compartidos -> {
                    tvWelcome.visibility = android.view.View.GONE
                    cargarFragmento(GastosCompartidosFragment())
                }
                R.id.nav_configuracion -> {
                    tvWelcome.visibility = android.view.View.GONE
                    cargarFragmento(AjustesFragment())
                }
            }
            true
        }
    }

    //CONFIGURACIÓN DE ACCIÓN AL PULSAR 2 VECES EL BOTÓN DE SALIR PARA QUE NO VAYA A LOGIN DE NUEVO
    private fun configurarBotonSalir() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (Salir) {
                    finishAffinity() // Cierra la actividad y la app
                    return
                }

                Salir = true
                Toast.makeText(
                    this@HomeActivity,
                    "Pulsa atrás de nuevo para salir",
                    Toast.LENGTH_SHORT
                ).show()

                // Si no pulsa en 2 segundos, reseteamos
                window.decorView.postDelayed({ Salir = false }, 2000)
            }
        })
    }

    // CARGAMOS EL FRAGMENTO QUE CONTIENE TODAS LAS FUNCIONES INTERACTIVAS
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // SE EL USUARIO ACTUALIZA SU NOMBRE LO HAREMOS TAMBIÉN EN EL HEADER GLOBAL
    fun actualizarNombreHeader(nuevoNombre: String) {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Hola, $nuevoNombre" // O el formato que uses
    }


}