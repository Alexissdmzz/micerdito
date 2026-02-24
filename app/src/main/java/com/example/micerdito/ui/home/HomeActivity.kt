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
import androidx.activity.viewModels
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.fragments.GastosCompartidosFragment
import com.example.micerdito.ui.fragments.GastosFragment
import com.example.micerdito.viewmodel.home.HomeViewModel

/**
 * ACTIVITY - HomeActivity:
 * Actúa como el host principal de la aplicación tras el login. Gestiona el contenedor
 * de fragmentos, el menú de navegación inferior (Bottom Navigation) y la lógica
 * de accesibilidad global (Temas).
 */
class HomeActivity : AppCompatActivity() {

    // Inicialización del ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Control de estado para la lógica de doble pulsación al salir
    private var Salir = false

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * APLICACIÓN DE TEMAS DINÁMICOS:
         * Se debe realizar ANTES de super.onCreate para que los recursos
         * de color se carguen correctamente en toda la jerarquía de vistas.
         */
        if (viewModel.esDaltonico()) {
            setTheme(R.style.Theme_MiCerdito_Daltonico)
        } else {
            setTheme(R.style.Theme_MiCerdito)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Mostramos la vista


        // Inicialización de componentes de la vista
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        configurarBotonSalir()

        // Inicialización del Header con el nombre persistido en SharedPreferences
        tvWelcome.text = "Hola, ${viewModel.obtenerNombreUsuario()}"

        /**
         * CARGA INICIAL:
         * Si no hay un estado guardado (primer arranque), cargamos el HomeFragment.
         */
        if (savedInstanceState == null) {
            cargarFragmento(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        /**
         * NAVEGACIÓN INFERIOR (Footer):
         * Gestiona el intercambio de fragmentos y la visibilidad del Header global.
         */
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    tvWelcome.visibility = android.view.View.VISIBLE
                    cargarFragmento(HomeFragment())
                }
                //R.id.nav_calendario
                R.id.nav_anadir_gasto -> {
                    tvWelcome.visibility = android.view.View.GONE
                    cargarFragmento(GastosFragment())
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

    /**
     * GESTIÓN DEL BOTÓN ATRÁS:
     * Implementa un callback para evitar cierres accidentales.
     * Requiere que el usuario pulse dos veces en un intervalo de 2 segundos.
     */
    private fun configurarBotonSalir() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (Salir) {
                    finishAffinity() // Cierra todas las actividades y sale de la App
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

    /**
     * TRANSACCIÓN DE FRAGMENTOS:
     * Sustituye el contenido del FrameLayout 'fragment_container' por el nuevo Fragmento.
     */
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * MÉTODO PÚBLICO DE COMUNICACIÓN:
     * Permite que fragmentos hijos (como AjustesFragment) actualicen el texto
     * del Header de la actividad principal tras un cambio de perfil.
     */
    fun actualizarNombreHeader(nuevoNombre: String) {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Hola, $nuevoNombre" // O el formato que uses
    }
}