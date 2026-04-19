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
 * FRAGMENTO - AjustesFragment
 * Gestiona la configuración del perfil del usuario, las preferencias visuales
 * y el cierre de sesión seguro.
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    // Conexión con el ViewModel para gestionar los datos y el estado
    private val viewModel: AjustesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vinculación de los componentes de la vista
        val btnLogout = view.findViewById<TextView>(R.id.btnLogout)
        val btnPerfil = view.findViewById<TextView>(R.id.btnPerfil)
        val btnBorrarCuenta = view.findViewById<TextView>(R.id.btnBorrarCuenta)

        setupObservers()
        setupListeners(btnPerfil, btnBorrarCuenta, btnLogout)
        configurarModosVisuales(view)
    }

    /**
     * Escucha las respuestas del servidor tras solicitar la edición o borrado de la cuenta.
     */
    private fun setupObservers() {
        viewModel.ajustesResult.observe(viewLifecycleOwner) { response ->
            if (response == null) {
                return@observe
            }

            // Verificamos qué operación acaba de terminar basándonos en la última acción registrada
            if (response.success) {
                when (viewModel.ultimaAccion) {
                    "EDITAR" -> {
                        val nuevoNombre = viewModel.nombreTemporal
                        // Sincronizamos el nuevo nombre con la cabecera de la pantalla principal
                        (activity as? HomeActivity)?.actualizarNombreHeader(nuevoNombre)
                        Toast.makeText(requireContext(), "¡Nombre actualizado!", Toast.LENGTH_SHORT)
                            .show()

                        // Limpiamos el estado para evitar ejecuciones duplicadas
                        viewModel.limpiarResultado()
                    }

                    "BORRAR" -> {
                        Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT)
                            .show()
                        irAlWelcome()
                        viewModel.limpiarResultado()
                    }
                }
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }

            // Limpieza de seguridad en caso de error
            viewModel.limpiarResultado()
        }
    }

    /**
     * Configura los clics para mostrar las alertas de confirmación.
     */
    private fun setupListeners(
        btnPerfil: TextView,
        btnBorrarCuenta: TextView,
        btnLogout: TextView
    ) {
        btnPerfil.setOnClickListener { mostrarConfirmacionEditarNombre() }
        btnBorrarCuenta.setOnClickListener { mostrarConfirmacionBorrado() }
        btnLogout.setOnClickListener { mostrarConfirmacionSalida() }
    }

    /**
     * PREFERENCIAS VISUALES:
     * Gestiona el cambio dinámico entre el tema claro y oscuro de la aplicación.
     */
    private fun configurarModosVisuales(view: View) {
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)

        // Sincronización del estado visual con la memoria del dispositivo
        switchDarkMode.isChecked = viewModel.esModoOscuro()
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setModoOscuro(isChecked)
            val modo =
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

            if (AppCompatDelegate.getDefaultNightMode() != modo) {
                AppCompatDelegate.setDefaultNightMode(modo)
            }
        }
    }

    /**
     * DIÁLOGOS DE CONFIRMACIÓN:
     * Ventanas emergentes para evitar que el usuario realice acciones destructivas por error.
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
     * Limpia la sesión actual y devuelve al usuario a la pantalla de bienvenida.
     */
    private fun irAlWelcome() {
        viewModel.cerrarSesion()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)

        // Limpieza total del historial para impedir el retorno mediante gestos o botones físicos
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Oculta la cabecera principal al entrar en la sección de configuración
        (activity as? HomeActivity)?.mostrarHeader(false)
    }
}