<?php

/**
 * API: Inserción de Gasto
 * Gestiona el registro de nuevos gastos y la validación de los datos recibidos.
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
require_once '../utils/subir_imagen.php';
require_once '../utils/auth.php';

// Aseguramos comunicación en UTF-8
$conexion->set_charset("utf8mb4");

// Recogida y normalización de datos
$id_usuario   = trim($_POST['id_usuario'] ?? '');
$id_categoria = trim($_POST['id_categoria'] ?? '');
$titulo       = trim($_POST['titulo'] ?? '');
$importe      = trim($_POST['importe'] ?? '');
$fecha_gasto  = trim($_POST['fecha_gasto'] ?? '');
$descripcion  = trim($_POST['descripcion'] ?? '');
$nombre_foto  = null;

// Validación de datos obligatorios
if (empty($id_usuario) || empty($id_categoria) || empty($titulo) || empty($importe) || empty($fecha_gasto)) {
    responderError("Faltan campos obligatorios para registrar el gasto.", 400);
}

if (!ctype_digit($id_usuario) || !ctype_digit($id_categoria)) {
    responderError("Identificadores inválidos.", 400);
}

if (!is_numeric($importe) || (float)$importe <= 0) {
    responderError("El importe debe ser un número válido mayor que 0.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de registrar el gasto.
 */
validarUsuarioExistente($conexion, $id_usuario);

/**
 * Gestión de la foto:
 * Si el usuario adjunta una imagen, se procesa y se guarda en el servidor.
 */
/*
// BLOQUE ANTIGUO
if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {
    $directorio_subida = "../uploads/tickets/";

    if (!file_exists($directorio_subida)) {
        mkdir($directorio_subida, 0777, true);
    }

    $extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
    $nombre_foto = "TK_" . $id_usuario . "_" . time() . "." . $extension;
    $ruta_final = $directorio_subida . $nombre_foto;

    if (!move_uploaded_file($_FILES['foto']['tmp_name'], $ruta_final)) {
        $nombre_foto = null;
    }
}
*/

// BLOQUE NUEVO
if (isset($_FILES['foto']) && $_FILES['foto']['error'] !== UPLOAD_ERR_NO_FILE) {
    $nombre_foto = guardarImagenTicket($_FILES['foto'], 'TK_' . $id_usuario);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_insertar_gasto(?, ?, ?, ?, ?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al preparar la inserción del gasto.
 */
error_log("Error en insertar_gasto.php al preparar sp_insertar_gasto: " . $conexion->error);

$stmt->bind_param("sssdsss", $id_usuario, $id_categoria, $titulo, $importe, $fecha_gasto, $descripcion, $nombre_foto);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al ejecutar la inserción del gasto.
 */
error_log("Error en insertar_gasto.php al ejecutar sp_insertar_gasto para id_usuario {$id_usuario}: " . $stmt->error);

$res = $stmt->get_result();

if (!$res) {
    /**
     * Logging interno:
     * Registramos el fallo al recuperar el resultado del procedimiento.
     */
    error_log("Error en insertar_gasto.php al recuperar resultados de sp_insertar_gasto para id_usuario {$id_usuario}.");
}

$id_gasto = null;

if ($res && $res->num_rows > 0) {
    $fila = $res->fetch_assoc();
    $id_gasto = $fila['id_gasto'] ?? null;
    $res->free();
}

// Cierre de la sentencia
$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

$respuesta = [
    "foto_ticket" => $nombre_foto
];

if (!empty($id_gasto)) {
    $respuesta["id_gasto"] = (string)$id_gasto;
}

responderExito("Gasto registrado correctamente.", $respuesta);
