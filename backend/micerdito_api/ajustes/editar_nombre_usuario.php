<?php

/**
 * API: Edición de Nombre de Usuario
 * Gestiona la actualización del nombre de usuario de un perfil existente.
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
$nombre_usuario = trim($_POST['nombre_usuario'] ?? '');

if (empty($id_usuario) || empty($nombre_usuario)) {
    responderError("Faltan datos obligatorios.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

if (!preg_match("/^[\p{L}0-9\s]+$/u", $nombre_usuario)) {
    responderError("El nombre contiene caracteres no válidos.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_editar_nom_usu(?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("ss", $id_usuario, $nombre_usuario);

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

responderExito("Nombre actualizado correctamente.", [
    "nombre_usuario" => (string)$nombre_usuario
]);
