<?php

require_once __DIR__ . '/respuesta.php';

function validarUsuarioExistente(mysqli $conexion, string $id_usuario): void
{
    if (!ctype_digit($id_usuario)) {
        responderError("Identificador de usuario inválido.", 400);
    }

    $stmt = $conexion->prepare("CALL sp_validar_usuario(?)");

    if (!$stmt) {
        responderError("Error interno del servidor", 500);
    }

    $stmt->bind_param("i", $id_usuario);

    if (!$stmt->execute()) {
        $stmt->close();
        responderError("Error interno del servidor", 500);
    }

    $res = $stmt->get_result();

    if (!$res) {
        $stmt->close();
        responderError("Error interno del servidor", 500);
    }

    if ($res->num_rows === 0) {
        $res->free();
        $stmt->close();

        // IMPORTANTE limpiar resultados pendientes
        while ($conexion->next_result()) {
            $conexion->store_result();
        }

        responderError("Usuario no válido.", 403);
    }

    $res->free();
    $stmt->close();

    // LIMPIEZA OBLIGATORIA por usar SP
    while ($conexion->next_result()) {
        $conexion->store_result();
    }
}

/**
 * Validación de autorización:
 * Comprueba que el gasto exista y pertenezca realmente al usuario autenticado.
 */
function validarGastoDeUsuario(mysqli $conexion, string $id_gasto, string $id_usuario): void
{
    if (!ctype_digit($id_gasto) || !ctype_digit($id_usuario)) {
        responderError("Identificadores inválidos.", 400);
    }

    $stmt = $conexion->prepare("CALL sp_validar_gasto_usuario(?, ?)");

    if (!$stmt) {
        responderError("Error interno del servidor", 500);
    }

    $stmt->bind_param("ii", $id_gasto, $id_usuario);

    if (!$stmt->execute()) {
        $stmt->close();
        responderError("Error interno del servidor", 500);
    }

    $res = $stmt->get_result();

    if (!$res) {
        $stmt->close();
        responderError("Error interno del servidor", 500);
    }

    if ($res->num_rows === 0) {
        $res->free();
        $stmt->close();

        while ($conexion->next_result()) {
            $conexion->store_result();
        }

        responderError("No autorizado para operar con este gasto.", 403);
    }

    $res->free();
    $stmt->close();

    while ($conexion->next_result()) {
        $conexion->store_result();
    }
}
