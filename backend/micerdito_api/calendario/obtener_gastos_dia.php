<?php

/**
 * API: Obtener Gastos por Día
 * Recupera la lista detallada de gastos de un usuario para una fecha específica.
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

// Aseguramos comunicación en UTF-8
$conexion->set_charset("utf8mb4");

// Recogida y normalización de datos
$id_usuario = trim($_GET['id_usuario'] ?? '');
$anio       = trim($_GET['anio'] ?? '');
$mes        = trim($_GET['mes'] ?? '');
$dia        = trim($_GET['dia'] ?? '');

if (empty($id_usuario) || empty($anio) || empty($mes) || empty($dia)) {
    responderError("Faltan parámetros para consultar los gastos del día.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

if (!ctype_digit($anio) || (int)$anio < 2000 || (int)$anio > 2100) {
    responderError("Año inválido.", 400);
}

if (!ctype_digit($mes) || (int)$mes < 1 || (int)$mes > 12) {
    responderError("Mes inválido.", 400);
}

if (!ctype_digit($dia) || (int)$dia < 1 || (int)$dia > 31) {
    responderError("Día inválido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de consultar sus gastos.
 */
validarUsuarioExistente($conexion, $id_usuario);

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_gastos_dia(?, ?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al preparar la consulta de gastos por día.
 */
error_log("Error en obtener_gastos_dia.php al preparar sp_obtener_gastos_dia: " . $conexion->error);

$stmt->bind_param("siii", $id_usuario, $anio, $mes, $dia);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al ejecutar la consulta de gastos por día.
 */
error_log("Error en obtener_gastos_dia.php al ejecutar sp_obtener_gastos_dia para id_usuario {$id_usuario}, fecha {$anio}-{$mes}-{$dia}: " . $stmt->error);

$res = $stmt->get_result();

if (!$res) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

/**
 * Logging interno:
 * Registramos el fallo al recuperar el resultset del procedimiento.
 */
error_log("Error en obtener_gastos_dia.php al recuperar resultados para id_usuario {$id_usuario}, fecha {$anio}-{$mes}-{$dia}.");

$gastos = [];

while ($fila = $res->fetch_assoc()) {
    $gastos[] = [
        "id_gasto"        => $fila['id_gasto'],
        "titulo"          => $fila['titulo'],
        "importe"         => (float)$fila['importe'],
        "descripcion"     => $fila['descripcion'],
        "fecha_gasto"     => $fila['fecha_gasto'],
        "icono_categoria" => $fila['icono_categoria'],
        "color_categoria" => $fila['color_categoria'],
        "foto_ticket"     => $fila['foto_ticket']
    ];
}

$res->free();

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    if ($tmp = $conexion->store_result()) {
        $tmp->free();
    }
}

responderExito(
    count($gastos) > 0 ? "Gastos recuperados correctamente." : "No hay gastos para este día.",
    [
        "data" => $gastos
    ]
);
