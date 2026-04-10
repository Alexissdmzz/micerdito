<?php

/**
 * API: Obtener Resumen de Calendario
 * Recupera la fecha de registro del usuario, los días con actividad
 * y los datos necesarios para el gráfico mensual.
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
$id_usuario = trim($_GET['id_usuario'] ?? '');
$mes        = trim($_GET['mes'] ?? '');
$anio       = trim($_GET['anio'] ?? '');

if (empty($id_usuario) || empty($mes) || empty($anio)) {
    responderError("Faltan parámetros para consultar el calendario.", 400);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_obtener_datos_calendario(?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("sii", $id_usuario, $mes, $anio);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

// --- 1. RESULTADO: Fecha de Registro ---
$res1 = $stmt->get_result();

if (!$res1) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

$fecha_registro = null;

if ($fila = $res1->fetch_assoc()) {
    $fecha_registro = $fila['fecha_registro_usuario'] ?? null;
}

$res1->free();

// --- 2. RESULTADO: Días con Gastos ---
$dias_con_gastos = [];

if ($stmt->next_result()) {
    $res2 = $stmt->get_result();

    if ($res2) {
        while ($fila = $res2->fetch_assoc()) {
            $dias_con_gastos[] = (int)$fila['dia'];
        }
        $res2->free();
    }
}

// --- 3. RESULTADO: Resumen del Gráfico ---
$resumen_grafico = [];

if ($stmt->next_result()) {
    $res3 = $stmt->get_result();

    if ($res3) {
        while ($fila = $res3->fetch_assoc()) {
            $resumen_grafico[] = [
                "nombre" => $fila['nombre_categoria'],
                "color"  => $fila['color_categoria'],
                "total"  => (float)$fila['total']
            ];
        }
        $res3->free();
    }
}

// Cierre de la sentencia
$stmt->close();

// Limpieza de resultados pendientes
while ($conexion->next_result()) {
    if ($tmp = $conexion->store_result()) {
        $tmp->free();
    }
}

// Respuesta final
responderExito("Datos recuperados correctamente.", [
    "fecha_registro"   => $fecha_registro,
    "dias_con_gastos"  => $dias_con_gastos,
    "resumen_grafico"  => $resumen_grafico
]);
