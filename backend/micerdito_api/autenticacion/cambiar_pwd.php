<?php

/**
 * API - Recuperación y Cambio de Contraseña
 * Verifica la respuesta de seguridad y actualiza la contraseña del usuario.
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
$correo = trim($_POST['correo'] ?? '');
$nueva_pwd_plana = $_POST['nueva_pwd'] ?? '';
$respuesta_usuario = trim($_POST['respuesta_seguridad'] ?? '');

// Validación básica
if (empty($correo) || empty($respuesta_usuario) || empty($nueva_pwd_plana)) {
    responderError("Faltan datos obligatorios.", 400);
}

// Normalización
$correo = mb_strtolower($correo, 'UTF-8');
$respuesta_usuario = mb_strtolower($respuesta_usuario, 'UTF-8');

// Validación de email
if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    responderError("El formato del correo electrónico no es válido.", 400);
}

// Validación de seguridad de contraseña
$pattern = '/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/';
if (!preg_match($pattern, $nueva_pwd_plana)) {
    responderError(
        "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.",
        400
    );
}

// 1) Recuperamos el hash de la respuesta de seguridad
$stmt = $conexion->prepare("CALL sp_recuperar_pregunta(?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $correo);

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
    responderError("Usuario no encontrado.", 404);
}

$fila = $res->fetch_assoc();
$res->free();
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

$hash_db = $fila['respuesta_seguridad'] ?? null;

if (empty($hash_db)) {
    responderError("Respuesta inesperada del servidor.", 500);
}

// 2) Verificamos la respuesta de seguridad
if (!password_verify($respuesta_usuario, $hash_db)) {
    responderError("La respuesta de seguridad es incorrecta.", 401);
}

// 3) Actualizamos la nueva contraseña
$nueva_pwd_hash = password_hash($nueva_pwd_plana, PASSWORD_DEFAULT);

$update = $conexion->prepare("CALL sp_cambiar_pwd(?, ?)");

if (!$update) {
    responderError("Error interno del servidor", 500);
}

$update->bind_param("ss", $correo, $nueva_pwd_hash);

if (!$update->execute()) {
    $update->close();
    responderError("Error interno del servidor", 500);
}

$update->close();

while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("¡Contraseña actualizada con éxito!");
