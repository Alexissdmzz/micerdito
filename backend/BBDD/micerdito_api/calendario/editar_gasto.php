<?php

/**
 * API: Edición de Gasto
 * Actualiza los detalles de un gasto existente (título, importe, descripción y foto).
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
$id_gasto    = trim($_POST['id_gasto'] ?? '');
$titulo      = trim($_POST['titulo'] ?? '');
$importe     = trim($_POST['importe'] ?? '');
$descripcion = trim($_POST['descripcion'] ?? '');
$nombre_foto_final = trim($_POST['foto_ticket'] ?? '');

// Validación de datos obligatorios
if (empty($id_gasto) || empty($titulo) || $importe === '') {
    responderError("Faltan parámetros obligatorios para editar.", 400);
}

/**
 * Gestión de la foto:
 * Si el usuario sube una nueva imagen, se procesa y sustituye.
 */
if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {

    $directorio_subida = "../uploads/tickets/";

    if (!file_exists($directorio_subida)) {
        mkdir($directorio_subida, 0777, true);
    }

    $extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
    $nuevo_nombre = "TK_EDIT_" . $id_gasto . "_" . time() . "." . $extension;
    $ruta_final = $directorio_subida . $nuevo_nombre;

    if (move_uploaded_file($_FILES['foto']['tmp_name'], $ruta_final)) {
        $nombre_foto_final = $nuevo_nombre;
    }
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_editar_gasto(?, ?, ?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("ssdss", $id_gasto, $titulo, $importe, $descripcion, $nombre_foto_final);

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
    responderError($fila['message'] ?? "No se pudo editar el gasto.", 400);
}

// Respuesta exitosa
responderExito($fila['message'] ?? "Gasto actualizado correctamente.", [
    "id_gasto" => (string)$id_gasto,
    "foto_ticket" => (string)$nombre_foto_final
]);
