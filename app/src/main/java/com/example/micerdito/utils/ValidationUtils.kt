package com.example.micerdito.utils

/**
 * UTILIDAD - ValidationUtils
 * Componente transversal dedicado a la validación y saneamiento de datos de entrada.
 * Centraliza la lógica de transformación de formatos numéricos introducidos por el usuario
 * para prevenir errores de conversión o registros financieros inválidos en la base de datos.
 */
object ValidationUtils {

    /**
     * Transforma una cadena de texto en un valor numérico de coma flotante seguro.
     * Normaliza los separadores decimales (convirtiendo comas en puntos) y garantiza
     * mediante reglas de negocio que el importe resultante sea estrictamente mayor a cero.
     * * @param input Cadena de texto capturada directamente desde los formularios de la interfaz.
     * @return El valor numérico verificado, o un valor nulo si la cadena no representa una cifra válida.
     */
    fun parsePositiveAmount(input: String): Double? {
        // Fase 1: Saneamiento de la cadena
        val normalized = input
            .trim()
            .replace(",", ".")

        // Fase 2: Conversión segura a tipo numérico
        val value = normalized.toDoubleOrNull() ?: return null

        // Fase 3: Validación de regla de negocio (importe estrictamente positivo)
        return if (value > 0) value else null
    }
}