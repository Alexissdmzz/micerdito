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

// Recogida y normalización de datos
$id_usuario = trim($_POST['id_usuario'] ?? '');
$limite     = trim($_POST['limite'] ?? '');

if (empty($id_usuario) || $limite === '') {
    responderError("Faltan datos obligatorios.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_guardar_limite(?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("sd", $id_usuario, $limite);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("Límite actualizado correctamente.", [
    "limite" => (float)$limite
]);
