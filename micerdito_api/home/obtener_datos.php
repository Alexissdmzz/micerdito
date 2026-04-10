<?php

/**
 * API: Obtención de Datos para Home
 * Recupera la información necesaria para construir la interfaz principal de la aplicación.
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

// Configuración regional para fechas en español
$conexion->query("SET lc_time_names = 'es_ES'");

// Recogida y normalización de datos
$id_usuario = trim($_POST['id_usuario'] ?? '');

if (empty($id_usuario)) {
    responderError("ID de usuario no proporcionado.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_datos(?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $id_usuario);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

$res = $stmt->get_result();

if (!$res) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

$datos = $res->fetch_assoc();
$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

if (!$datos) {
    responderError("Usuario no encontrado.", 404);
}

responderExito("Datos recuperados correctamente.", $datos);
