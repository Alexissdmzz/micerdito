<?php

/**
 * API: Obtención de Categorías
 * Recupera la lista de categorías registradas en la base de datos.
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

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_categorias()");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

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

$categorias = [];

while ($fila = $res->fetch_assoc()) {
    $categorias[] = $fila;
}

$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("Categorías recuperadas correctamente.", [
    "categorias" => $categorias
]);
