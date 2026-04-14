<?php

/**
 * API: Obtención de Movimientos
 * Recupera la lista de los últimos movimientos registrados por el usuario.
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

if (empty($id_usuario)) {
    responderError("ID de usuario no proporcionado.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_movimientos(?)");

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

$movimientos = [];

while ($fila = $res->fetch_assoc()) {
    $movimientos[] = $fila;
}

$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("Movimientos recuperados correctamente.", [
    "gastos_recientes" => $movimientos
]);
