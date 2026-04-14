<?php

/**
 * API: Obtención de Pregunta de Seguridad
 * Recupera el texto de la pregunta de seguridad vinculada a un correo electrónico.
 * Este es el primer paso del flujo de recuperación de cuenta.
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
$correo = trim($_POST['correo'] ?? '');

if (empty($correo)) {
    responderError("Por favor, introduce tu correo electrónico.", 400);
}

$correo = mb_strtolower($correo, 'UTF-8');

if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    responderError("El formato del correo electrónico no es válido.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_pregunta(?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $correo);

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

if ($res->num_rows === 0) {
    $res->free();
    $stmt->close();
    responderError("No se puedo recuperar la pregunta de seguridad.", 404);
}

$fila = $res->fetch_assoc();
$res->free();

// Limpiamos resultados pendientes del procedimiento
$stmt->close();
while ($conexion->next_result()) {
    $conexion->store_result();
}

// Validamos que la pregunta exista realmente
if (!isset($fila['pregunta']) || empty($fila['pregunta'])) {
    responderError("El usuario no tiene una pregunta registrada.", 404);
}

responderExito("Pregunta recuperada correctamente.", [
    "pregunta" => (string)$fila['pregunta']
]);
