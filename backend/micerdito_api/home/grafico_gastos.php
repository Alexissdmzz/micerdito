<?php

/**
 * API: Obtención de Gastos para Gráfico
 * Recupera el desglose de gastos por categoría del mes actual para el panel principal.
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
$id_usuario = trim($_GET['id_usuario'] ?? '');

if (empty($id_usuario)) {
    responderError("ID de usuario no proporcionado.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de obtener sus datos del gráfico.
 */
validarUsuarioExistente($conexion, $id_usuario);

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_gastos_grafico(?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al preparar la consulta del gráfico de gastos.
 */
error_log("Error en obtener_gastos_grafico.php al preparar sp_obtener_gastos_grafico: " . $conexion->error);

$stmt->bind_param("s", $id_usuario);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al ejecutar la consulta del gráfico de gastos.
 */
error_log("Error en obtener_gastos_grafico.php al ejecutar sp_obtener_gastos_grafico para id_usuario {$id_usuario}: " . $stmt->error);

$res = $stmt->get_result();

if (!$res) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

/**
 * Logging interno:
 * Registramos el fallo al recuperar el resultset del procedimiento.
 */
error_log("Error en obtener_gastos_grafico.php al recuperar resultados para id_usuario {$id_usuario}.");

$datos = [];

while ($fila = $res->fetch_assoc()) {
    $datos[] = [
        "nombre"     => $fila['nombre'],
        "totalGasto" => (float)$fila['totalGasto'],
        "color"      => $fila['color']
    ];
}

$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderExito("Datos del gráfico recuperados correctamente.", [
    "datos" => $datos
]);
