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
import androidx.appcompat.app.AppCompatDelegate
import com.example.micerdito.ui.fragments.CalendarioFragment
import com.example.micerdito.ui.fragments.GastosCompartidosFragment
import com.example.micerdito.ui.fragments.GastosFragment
import com.example.micerdito.viewmodel.home.HomeViewModel

/**
 * ACTIVITY - HomeActivity:
 * Actúa como el host principal de la aplicación tras el login. Gestiona el contenedor
 * de fragmentos, el menú de navegación inferior (Bottom Navigation) y la lógica
 * de accesibilidad global (Temas y Modo Oscuro).
 */
class HomeActivity : AppCompatActivity() {

    // Inicialización del ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Control de estado para la lógica de doble pulsación al salir
    private var Salir = false

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * CONFIGURACIÓN VISUAL GLOBAL:
         * Leemos las preferencias del usuario antes de crear la vista para evitar
         * parpadeos o saltos de color.
         */
        aplicarConfiguracionVisual()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicialización de componentes de la vista
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        configurarBotonSalir()

        // Inicialización del Header con el nombre persistido
        tvWelcome.text = "Hola, ${viewModel.obtenerNombreUsuario()}"

        /**
         * CARGA INICIAL:
         * Si es el primer arranque, cargamos el HomeFragment por defecto.
         */
        if (savedInstanceState == null) {
            cargarFragmento(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        /**
         * NAVEGACIÓN INFERIOR (Footer):
         * Intercambio de fragmentos y gestión de la visibilidad del Header.
         */
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    tvWelcome.visibility = android.view.View.VISIBLE
                    cargarFragmento(HomeFragment())
                }
                R.id.nav_calendario -> {
                    tvWelcome.visibility = android.view.View.GONE
                    cargarFragmento(CalendarioFragment())
                }
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
     * GESTIÓN DE TEMAS Y ACCESIBILIDAD:
     * Aplica el modo daltónico y el modo oscuro recuperando los valores del ViewModel.
     */
    private fun aplicarConfiguracionVisual() {
        // 1. Aplicamos el Tema (Daltonismo vs Normal)
        if (viewModel.esDaltonico()) {
            setTheme(R.style.Theme_MiCerdito_Daltonico)
        } else {
            setTheme(R.style.Theme_MiCerdito)
        }

        // 2. Aplicamos el Modo Oscuro (Noche vs Día)
        val modoNoche = if (viewModel.esModoOscuro()) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(modoNoche)
    }

    /**
     * GESTIÓN DEL BOTÓN ATRÁS:
     * Evita cierres accidentales mediante doble pulsación.
     */
    private fun configurarBotonSalir() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (Salir) {
                    finishAffinity()
                    return
                }

                Salir = true
                Toast.makeText(
                    this@HomeActivity,
                    "Pulsa atrás de nuevo para salir",
                    Toast.LENGTH_SHORT
                ).show()

                window.decorView.postDelayed({ Salir = false }, 2000)
            }
        })
    }

    /**
     * TRANSACCIÓN DE FRAGMENTOS:
     * Reemplaza el contenedor principal por el fragmento seleccionado.
     */
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * ACTUALIZACIÓN DE INTERFAZ:
     * Sincroniza el nombre del usuario en el Header tras cambios en el perfil.
     */
    fun actualizarNombreHeader(nuevoNombre: String) {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Hola, $nuevoNombre"
    }
}