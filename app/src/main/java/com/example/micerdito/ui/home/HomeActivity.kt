package com.example.micerdito.ui.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.micerdito.R
import com.example.micerdito.ui.fragments.AjustesFragment
import com.example.micerdito.ui.fragments.CalendarioFragment
import com.example.micerdito.ui.fragments.GastosFragment
import com.example.micerdito.ui.fragments.HomeFragment
import com.example.micerdito.viewmodel.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * ACTIVITY - HomeActivity:
 * Actúa como el host principal de la aplicación tras el login. Gestiona el contenedor
 * de fragmentos, el menú de navegación inferior (Bottom Navigation) y la lógica global.
 */
class HomeActivity : AppCompatActivity() {

    // Inicialización del ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Control de estado para la lógica de doble pulsación al salir
    private var salir = false

    override fun onCreate(savedInstanceState: Bundle?) {
        aplicarConfiguracionVisual()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(android.R.id.content)
        val headerContainer = findViewById<View>(R.id.headerContainer)
        val fragmentContainer = findViewById<View>(R.id.fragment_container)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        configurarInsets(root, headerContainer, fragmentContainer, bottomNav)

        configurarBotonSalir()

        // Inicialización del header con el nombre persistido
        tvWelcome.text = "Bienvenido, ${viewModel.obtenerNombreUsuario()}"

        // Carga inicial
        if (savedInstanceState == null) {
            cargarFragmento(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

        // Navegación inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cargarFragmento(HomeFragment())
                R.id.nav_calendario -> cargarFragmento(CalendarioFragment())
                R.id.nav_anadir_gasto -> cargarFragmento(GastosFragment())
                R.id.nav_configuracion -> cargarFragmento(AjustesFragment())
            }
            true
        }
    }

    /**
     * GESTIÓN DE TEMAS:
     * Aplica el modo oscuro recuperando el valor desde el ViewModel.
     */
    private fun aplicarConfiguracionVisual() {
        setTheme(R.style.Theme_MiCerdito)

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
                if (salir) {
                    finishAffinity()
                    return
                }

                salir = true
                Toast.makeText(
                    this@HomeActivity,
                    "Pulsa atrás de nuevo para salir",
                    Toast.LENGTH_SHORT
                ).show()

                window.decorView.postDelayed({ salir = false }, 2000)
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
     * CONTROL DEL HEADER:
     * Muestra u oculta completamente el contenedor del header.
     */
    fun mostrarHeader(mostrar: Boolean) {
        val headerContainer = findViewById<View>(R.id.headerContainer)

        if (mostrar) {
            headerContainer.visibility = View.VISIBLE
            window.statusBarColor = ContextCompat.getColor(this, R.color.rosa_cerdito)
        } else {
            headerContainer.visibility = View.GONE
            window.statusBarColor = ContextCompat.getColor(this, R.color.background_light)
        }
    }

    /**
     * ACTUALIZACIÓN DE INTERFAZ:
     * Sincroniza el nombre del usuario en el header tras cambios en el perfil.
     */
    fun actualizarNombreHeader(nuevoNombre: String) {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Bienvenido, $nuevoNombre"
    }

    private fun configurarInsets(
        root: View,
        headerContainer: View,
        fragmentContainer: View,
        bottomNav: BottomNavigationView
    ) {
        val headerTopOriginal = headerContainer.paddingTop
        val headerLeftOriginal = headerContainer.paddingLeft
        val headerRightOriginal = headerContainer.paddingRight
        val headerBottomOriginal = headerContainer.paddingBottom

        val bottomLeftOriginal = bottomNav.paddingLeft
        val bottomTopOriginal = bottomNav.paddingTop
        val bottomRightOriginal = bottomNav.paddingRight
        val bottomBottomOriginal = bottomNav.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            headerContainer.updatePadding(
                left = headerLeftOriginal + systemBars.left,
                top = headerTopOriginal + systemBars.top,
                right = headerRightOriginal + systemBars.right,
                bottom = headerBottomOriginal
            )

            bottomNav.updatePadding(
                left = bottomLeftOriginal + systemBars.left,
                top = bottomTopOriginal,
                right = bottomRightOriginal + systemBars.right,
                bottom = bottomBottomOriginal + systemBars.bottom
            )

            fragmentContainer.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )

            insets
        }
    }
}