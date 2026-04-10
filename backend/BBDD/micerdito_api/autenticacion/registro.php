<?php

/**
 * API: Registro
 * Valida credenciales y da de alta al usuario.
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

// Recogida de datos enviados desde la App
$nombre_usuario = trim($_POST['nombre_usuario'] ?? '');
$correo = trim($_POST['correo'] ?? '');
$pwd = $_POST['pwd'] ?? '';
$repeat_pwd = $_POST['repeat_pwd'] ?? '';
$id_pregunta = $_POST['id_pregunta'] ?? '';
$respuesta = trim($_POST['respuesta_seguridad'] ?? '');

// Validación básica
if (
    empty($nombre_usuario) ||
    empty($correo) ||
    empty($pwd) ||
    empty($repeat_pwd) ||
    strlen((string)$id_pregunta) === 0 ||
    empty($respuesta)
) {
    responderError("Por favor, rellena todos los campos obligatorios.", 400);
}

// Normalización del correo
$correo = mb_strtolower($correo, 'UTF-8');

// Validaciones de formato
if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    responderError("El correo no tiene un formato válido.", 400);
}

if ($pwd !== $repeat_pwd) {
    responderError("Las contraseñas no coinciden.", 400);
}

// Hash de contraseña y respuesta de seguridad
$password_hash = password_hash($pwd, PASSWORD_DEFAULT);
$respuesta_hash = password_hash(mb_strtolower($respuesta, 'UTF-8'), PASSWORD_DEFAULT);

// Preparación de la llamada al SP
$stmt = $conexion->prepare("CALL sp_registro(?, ?, ?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$id_int = (int)$id_pregunta;
$stmt->bind_param("sssis", $nombre_usuario, $correo, $password_hash, $id_int, $respuesta_hash);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

$result = $stmt->get_result();

if (!$result) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

$datos = $result->fetch_assoc();

// Limpiamos resultados pendientes del procedimiento almacenado
$stmt->close();
while ($conexion->next_result()) {
    $conexion->store_result();
}

if (!$datos || !isset($datos['status'], $datos['message'])) {
    responderError("Respuesta inesperada del servidor.", 500);
}

if ($datos['status'] === 'success') {
    responderExito($datos['message'], [], 201);
}

if ($datos['status'] === 'error') {
    responderError($datos['message'], 409);
}

// Fallback por si el SP devolviera un estado no contemplado
responderError("Respuesta inesperada del servidor.", 500);
