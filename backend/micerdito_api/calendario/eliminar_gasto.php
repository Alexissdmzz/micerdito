<?php

/**
 * API: Eliminación de Gasto
 * Borra permanentemente un registro de gasto identificado por su UUID.
 */

// Limpiamos cualquier salida previa que pueda romper el JSON
if (ob_get_level()) ob_end_clean();
error_reporting(0);
ini_set('display_errors', 0);

// Comunicación JSON en UTF-8
header('Content-Type: application/json; charset=utf-8');

// Conexión y utilidades comunes
require_once '../conexion/conexion.php';
require_once '../utils/respuesta.php';
require_once '../utils/auth.php';

// Aseguramos comunicación en UTF-8
$conexion->set_charset("utf8mb4");

// Recogida y normalización de datos
$id_usuario = trim($_POST['id_usuario'] ?? '');
$id_gasto = trim($_POST['id_gasto'] ?? '');

if (empty($id_usuario) || empty($id_gasto)) {
    responderError("Faltan datos obligatorios para eliminar el gasto.", 400);
}

if (!preg_match('/^[a-f0-9-]+$/i', $id_gasto)) {
    responderError("Identificador de gasto inválido.", 400);
}

if (!preg_match('/^[a-f0-9-]+$/i', $id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de eliminar el gasto.
 */
validarUsuarioExistente($conexion, $id_usuario);

/**
 * Validación de autorización:
 * Comprobamos que el gasto exista y pertenezca realmente al usuario.
 */
validarGastoDeUsuario($conexion, $id_gasto, $id_usuario);

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_eliminar_gasto(?)");

if (!$stmt) {
    error_log("Error en eliminar_gasto.php al preparar sp_eliminar_gasto: " . $conexion->error);
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $id_gasto);

// Ejecución
if (!$stmt->execute()) {
    error_log("Error en eliminar_gasto.php al ejecutar sp_eliminar_gasto para id_gasto {$id_gasto} e id_usuario {$id_usuario}: " . $stmt->error);
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

$res = $stmt->get_result();

if (!$res) {
    error_log("Error en eliminar_gasto.php al recuperar resultados de sp_eliminar_gasto para id_gasto {$id_gasto}.");
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

$fila = $res->fetch_assoc();
$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    if ($tmp = $conexion->store_result()) {
        $tmp->free();
    }
}

// Validamos respuesta del procedimiento
if (!$fila || !isset($fila['success'])) {
    error_log("Error en eliminar_gasto.php: respuesta inesperada de sp_eliminar_gasto para id_gasto {$id_gasto}.");
    responderError("Respuesta inválida del servidor.", 500);
}

if ((bool)$fila['success'] === false) {
    responderError($fila['message'] ?? "No se pudo eliminar el gasto.", 400);
}

responderExito($fila['message'] ?? "Gasto eliminado correctamente.", [
    "id_gasto" => (string)$id_gasto
]);
