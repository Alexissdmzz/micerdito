<?php

/**
 * API: Creación de Límite de Gasto
 * Gestiona el registro o actualización de un límite de gasto mensual para un usuario.
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

// Recogida y normalización de datos
$id_usuario = trim($_POST['id_usuario'] ?? '');
$limite     = trim($_POST['limite'] ?? '');

if (empty($id_usuario) || $limite === '') {
    responderError("Faltan datos obligatorios.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

if (!is_numeric($limite) || (float)$limite < 0) {
    responderError("El límite debe ser un número válido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de guardar el límite.
 */
validarUsuarioExistente($conexion, $id_usuario);

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_guardar_limite(?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al preparar la actualización del límite.
 */
error_log("Error en guardar_limite.php al preparar sp_guardar_limite: " . $conexion->error);

$stmt->bind_param("sd", $id_usuario, $limite);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al ejecutar la actualización del límite.
 */
error_log("Error en guardar_limite.php al ejecutar sp_guardar_limite para id_usuario {$id_usuario}: " . $stmt->error);

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("Límite actualizado correctamente.", [
    "limite" => (float)$limite
]);
