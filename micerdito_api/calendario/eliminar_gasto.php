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

// Aseguramos comunicación en UTF-8
$conexion->set_charset("utf8mb4");

// Recogida y normalización de datos
$id_gasto = trim($_POST['id_gasto'] ?? '');

if (empty($id_gasto)) {
    responderError("ID de gasto no proporcionado.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_eliminar_gasto(?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $id_gasto);

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
    responderError("Respuesta inválida del servidor.", 500);
}

if ((bool)$fila['success'] === false) {
    responderError($fila['message'] ?? "No se pudo eliminar el gasto.", 400);
}

responderExito($fila['message'] ?? "Gasto eliminado correctamente.", [
    "id_gasto" => (string)$id_gasto
]);
