package com.example.micerdito.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.micerdito.R
import com.example.micerdito.ui.autenticacion.WelcomeActivity
import com.example.micerdito.ui.home.HomeActivity
import com.example.micerdito.viewmodel.home.AjustesViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * FRAGMENTO - AjustesFragment:
 * Gestiona la configuración del perfil del usuario, preferencias visuales de accesibilidad
 * y la finalización de la sesión.
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    // Inicialización del ViewModel
    private val viewModel: AjustesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultamos elementos de la Activity principal que no pertenecen a este contexto
        activity?.findViewById<View>(R.id.tvWelcome)?.visibility = View.GONE

        // Inicialización de componentes de la vista
        val btnLogout = view.findViewById<TextView>(R.id.btnLogout)
        val btnPerfil = view.findViewById<TextView>(R.id.btnPerfil)
        val btnBorrarCuenta = view.findViewById<TextView>(R.id.btnBorrarCuenta)

        // Configuración de observadores para reaccionar a cambios en el ViewModel
        setupObservers()

        // Carga de estados de interruptores (Switches)
        configurarModosVisuales(view)

        // Asignación de eventos de clic con cuadros de diálogo de confirmación
        btnPerfil.setOnClickListener { mostrarConfirmacionEditarNombre() }
        btnBorrarCuenta.setOnClickListener { mostrarConfirmacionBorrado() }
        btnLogout.setOnClickListener { mostrarConfirmacionSalida() }
    }

    /**
     * CONFIGURACIÓN DE OBSERVADORES:
     * Reacciona a los resultados de las peticiones de red (Editar/Borrar).
     */
    private fun setupObservers() {
        viewModel.ajustesResult.observe(viewLifecycleOwner) { response ->
            if (response == null) {
                return@observe
            }

            // El ViewModel guarda 'ultimaAccion' para saber qué proceso terminó
            if (response.success) {
                when (viewModel.ultimaAccion) {
                    "EDITAR" -> {
                        val nuevoNombre = viewModel.nombreTemporal
                        // Comunicación entre fragmentos: Actualizamos el nombre en el Navigation Drawer
                        (activity as? HomeActivity)?.actualizarNombreHeader(nuevoNombre)
                        Toast.makeText(requireContext(), "¡Nombre actualizado!", Toast.LENGTH_SHORT)
                            .show()

                        // Limpiamos el resultado para evitar que el observador se dispare al volver al fragmento
                        viewModel.limpiarResultado()
                    }

                    "BORRAR" -> {
                        Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT)
                            .show()
                        irAlWelcome() // Dirigimo al usuario a la Activity Welcome por defecto

                        // Limpiamos el resultado para evitar que el observador se dispare al volver al fragmento
                        viewModel.limpiarResultado()
                    }
                }
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
            viewModel.limpiarResultado()
        }
    }

    /**
     * ACCESIBILIDAD Y PERSONALIZACIÓN:
     * Gestiona el cambio dinámico de temas (Modo Oscuro) y modos de color (Daltonismo).
     */
    private fun configurarModosVisuales(view: View) {
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val switchDaltonismo = view.findViewById<SwitchMaterial>(R.id.switchDaltonismo)

        // Sincronización del estado con SharedPreferences a través del ViewModel
        switchDarkMode.isChecked = viewModel.esModoOscuro()
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setModoOscuro(isChecked)
            val modo =
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(modo)
        }

        // Modo Daltonismo
        switchDaltonismo.isChecked = viewModel.esDaltonico()
        switchDaltonismo.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDaltonico(isChecked)

            // Recreamos la Activity para forzar la aplicación de los nuevos recursos de color
            activity?.recreate()
        }
    }

    /**
     * DIÁLOGOS DE CONFIRMACIÓN:
     * Implementación de AlertDialog para prevenir acciones accidentales críticas.
     */
    private fun mostrarConfirmacionEditarNombre() {
        val dialogView = layoutInflater.inflate(R.layout.cambiar_nombre_usuario, null)
        val etNuevoNombre = dialogView.findViewById<EditText>(R.id.etNuevoNombre)

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar nombre")
            .setView(dialogView)
            .setPositiveButton("Cambiar") { _, _ ->
                val nombre = etNuevoNombre.text.toString().trim()
                if (nombre.isNotEmpty()) viewModel.editarUsuario(nombre)
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun mostrarConfirmacionBorrado() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Borrar cuenta?")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ -> viewModel.borrarCuenta() }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun mostrarConfirmacionSalida() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setPositiveButton("Salir") { _, _ -> irAlWelcome() }
            .setNegativeButton("Cancelar", null).show()
    }

    /**
     * NAVEGACIÓN DE SALIDA:
     * Limpia los datos de sesión y reinicia el stack de actividades hacia WelcomeActivity.
     */
    private fun irAlWelcome() {
        viewModel.cerrarSesion()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)

        // Limpiamos el historial para que no pueda volver atrás con el botón físico
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}