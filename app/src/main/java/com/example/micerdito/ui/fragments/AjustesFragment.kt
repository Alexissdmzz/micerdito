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
import com.example.micerdito.data.preferencias.PreferenciasSesion
import com.example.micerdito.ui.autenticacion.WelcomeActivity
import com.example.micerdito.ui.home.HomeActivity
import com.example.micerdito.viewmodel.home.AjustesViewModel
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * @AjustesFragment es la clase donde definimos los elementos interactivos del xml @fragment_ajustes para el usuario
 *  * en la pantalla de ajustes.
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    private val viewModel: AjustesViewModel by viewModels() // Herramienta que nos deja conectar con la Lógica (ViewModel)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.tvWelcome)?.visibility = View.GONE

        // INICIALIZAMOS LOS ELEMENTOS INTERACTIVOS
        val btnLogout = view.findViewById<TextView>(R.id.btnLogout)
        val btnPerfil = view.findViewById<TextView>(R.id.btnPerfil)
        val btnBorrarCuenta = view.findViewById<TextView>(R.id.btnBorrarCuenta)

        setupObservers() // Observador
        configurarModosVisuales(view)

        // EVENTOS CLICKS
        btnPerfil.setOnClickListener { mostrarConfirmacionEditarNombre() }
        btnBorrarCuenta.setOnClickListener { mostrarConfirmacionBorrado() }
        btnLogout.setOnClickListener { mostrarConfirmacionSalida() }
    }

    private fun setupObservers() {
        viewModel.ajustesResult.observe(viewLifecycleOwner) { response ->

            if (response == null) {
                return@observe
            }

            //  DEPENDIENDO DE LA ULTIMA ACCIÓN DEL VIEWMODEL REALIZAMOS UNA ACCIÓN U OTRA
            if (response.success) {
                when (viewModel.ultimaAccion) {
                    "EDITAR" -> {
                        val nuevoNombre = viewModel.nombreTemporal
                        (activity as? HomeActivity)?.actualizarNombreHeader(nuevoNombre)
                        Toast.makeText(requireContext(), "¡Nombre actualizado!", Toast.LENGTH_SHORT)
                            .show()

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
            viewModel.limpiarResultado()
        }
    }

    // EVENTOS VISUALES (NOCHE Y DALTONISMO)
    private fun configurarModosVisuales(view: View) {
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val switchDaltonismo = view.findViewById<SwitchMaterial>(R.id.switchDaltonismo)

        // Modo Oscuro
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
            activity?.recreate() // Recargamos para aplicar el nuevo tema
        }
    }

    // CONFIRMACIÓN AL DARLE AL BOTÓN DE CAMBIAR EL NOMBRE DE USUARIO
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

    // CONFIRMACIÓN AL DARLE AL BOTÓN DE ELIMINAR CUENTA
    private fun mostrarConfirmacionBorrado() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Borrar cuenta?")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ -> viewModel.borrarCuenta() }
            .setNegativeButton("Cancelar", null).show()
    }

    // CONFIRMACIÓN AL DARLE AL BOTÓN DE CERRRAR SESIÓN
    private fun mostrarConfirmacionSalida() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setPositiveButton("Salir") { _, _ -> irAlWelcome() }
            .setNegativeButton("Cancelar", null).show()
    }

    // VOLVER AL WELCOME Y BORRADO DE PREFERENCIAS
    private fun irAlWelcome() {
        viewModel.cerrarSesion() // Borra todo: ID, Nombre, login...
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}