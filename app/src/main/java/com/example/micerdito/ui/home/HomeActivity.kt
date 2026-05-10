package com.example.micerdito.ui.home

import android.graphics.Color
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
 * ACTIVITY - HomeActivity
 * Controlador principal de la interfaz post-autenticación.
 * Gestiona el contenedor dinámico de fragmentos, el enrutamiento del menú de navegación
 * inferior y la aplicación global de márgenes del sistema a pantalla completa.
 */
class HomeActivity : AppCompatActivity() {

    // Conexión con la lógica de negocio a nivel de actividad
    private val viewModel: HomeViewModel by viewModels()

    // Bandera de control para prevenir el cierre accidental de la aplicación
    private var salir = false

    override fun onCreate(savedInstanceState: Bundle?) {
        aplicarConfiguracionVisual()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Activamos el renderizado a pantalla completa
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Volvemos la barra de estado transparente para que las vistas se dibujen debajo
        window.statusBarColor = Color.TRANSPARENT

        val root = findViewById<View>(android.R.id.content)
        val headerContainer = findViewById<View>(R.id.headerContainer)
        val fragmentContainer = findViewById<View>(R.id.fragment_container)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)

        val colorIconos = ContextCompat.getColorStateList(this, R.color.nav_icon_color)
        bottomNav.itemIconTintList = colorIconos
        bottomNav.itemTextColor = colorIconos

        configurarInsets(root, headerContainer, fragmentContainer, bottomNav)
        configurarBotonSalir()

        tvWelcome.text = "Bienvenido, ${viewModel.obtenerNombreUsuario()}"

        if (savedInstanceState == null) {
            cargarFragmento(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }

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
     * Sincroniza la paleta de colores de la aplicación con la preferencia guardada en memoria.
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
     * Intercepta el evento de retroceso del dispositivo.
     * Requiere confirmación secuencial en un intervalo de dos segundos para finalizar el proceso.
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
     * Ejecuta la transacción de componentes de interfaz.
     */
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Controla la visibilidad de la cabecera principal y ajusta el color de los iconos
     * del sistema para garantizar el contraste visual.
     */
    fun mostrarHeader(mostrar: Boolean) {
        val headerContainer = findViewById<View>(R.id.headerContainer)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        if (mostrar) {
            headerContainer.visibility = View.VISIBLE
            // Forzamos iconos oscuros porque el fondo rosa de la cabecera es claro
            windowInsetsController.isAppearanceLightStatusBars = true
        } else {
            headerContainer.visibility = View.GONE
            // Si el header no está, el color de los iconos depende del modo oscuro
            windowInsetsController.isAppearanceLightStatusBars = !viewModel.esModoOscuro()
        }

        // Solicitamos al sistema operativo que recalcule los márgenes dinámicos
        findViewById<View>(android.R.id.content).requestApplyInsets()
    }

    /**
     * Sincroniza el componente de texto tras una actualización del perfil en la base de datos.
     */
    fun actualizarNombreHeader(nuevoNombre: String) {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Bienvenido, $nuevoNombre"
    }

    /**
     * Lógica dinámica de márgenes. Reparte de forma inteligente el espacio del sistema
     * (Barra de estado y barra de navegación) entre los contenedores de la aplicación.
     */
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

            // La barra de navegación inferior siempre absorbe su propio margen
            bottomNav.updatePadding(
                left = bottomLeftOriginal + systemBars.left,
                top = bottomTopOriginal,
                right = bottomRightOriginal + systemBars.right,
                bottom = bottomBottomOriginal + systemBars.bottom
            )

            // Distribución dinámica del margen superior (Barra de Estado)
            if (headerContainer.visibility == View.VISIBLE) {
                // El header asume el margen; el fragmento puede subir libremente
                headerContainer.updatePadding(
                    left = headerLeftOriginal + systemBars.left,
                    top = headerTopOriginal + systemBars.top,
                    right = headerRightOriginal + systemBars.right,
                    bottom = headerBottomOriginal
                )
                fragmentContainer.updatePadding(
                    left = systemBars.left,
                    top = 0,
                    right = systemBars.right
                )
            } else {
                // Sin header, el fragmento asume el margen para no quedar tapado
                fragmentContainer.updatePadding(
                    left = systemBars.left,
                    top = systemBars.top,
                    right = systemBars.right
                )
            }
            insets
        }
    }
}