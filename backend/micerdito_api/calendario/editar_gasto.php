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
require_once '../utils/subir_imagen.php';
require_once '../utils/auth.php';

// Aseguramos comunicación en UTF-8
$conexion->set_charset("utf8mb4");

// Recogida y normalización de datos
$id_usuario  = trim($_POST['id_usuario'] ?? '');
$id_gasto    = trim($_POST['id_gasto'] ?? '');
$titulo      = trim($_POST['titulo'] ?? '');
$importe     = trim($_POST['importe'] ?? '');
$descripcion = trim($_POST['descripcion'] ?? '');
$nombre_foto_final = trim($_POST['foto_ticket'] ?? '');

// Validación de datos obligatorios
if (empty($id_usuario) || empty($id_gasto) || empty($titulo) || $importe === '') {
    responderError("Faltan parámetros obligatorios para editar.", 400);
}

if (!ctype_digit($id_gasto)) {
    responderError("Identificador de gasto inválido.", 400);
}

if (!is_numeric($importe) || (float)$importe <= 0) {
    responderError("El importe debe ser un número válido mayor que 0.", 400);
}

if (!ctype_digit($id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de editar el gasto.
 */
validarUsuarioExistente($conexion, $id_usuario);

/**
 * Validación de autorización:
 * Comprobamos que el gasto exista y pertenezca realmente al usuario.
 */
validarGastoDeUsuario($conexion, $id_gasto, $id_usuario);

/**
 * Gestión de la foto:
 * Si el usuario sube una nueva imagen, se procesa y sustituye.
 */

/*
// ❌ BLOQUE ANTIGUO (se deja comentado por seguridad)

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
*/

// ✅ BLOQUE NUEVO (seguro)
if (isset($_FILES['foto']) && $_FILES['foto']['error'] !== UPLOAD_ERR_NO_FILE) {
    $nombre_foto_final = guardarImagenTicket($_FILES['foto'], 'TK_EDIT_' . $id_gasto);
}

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_editar_gasto(?, ?, ?, ?, ?)");

if (!$stmt) {
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al preparar la edición del gasto.
 */
error_log("Error en editar_gasto.php al preparar sp_editar_gasto: " . $conexion->error);

$stmt->bind_param("ssdss", $id_gasto, $titulo, $importe, $descripcion, $nombre_foto_final);

// Ejecución
if (!$stmt->execute()) {
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

/**
 * Logging interno:
 * Registramos el error al ejecutar la edición del gasto.
 */
error_log("Error en editar_gasto.php al ejecutar sp_editar_gasto para id_gasto {$id_gasto} e id_usuario {$id_usuario}: " . $stmt->error);

$res = $stmt->get_result();

if (!$res) {
    $stmt->close();
    responderError("Respuesta inesperada del servidor.", 500);
}

/**
 * Logging interno:
 * Registramos el fallo al recuperar el resultado del procedimiento.
 */
error_log("Error en editar_gasto.php al recuperar resultados de sp_editar_gasto para id_gasto {$id_gasto}.");

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

/**
 * Logging interno:
 * Registramos una respuesta inesperada devuelta por el procedimiento.
 */
error_log("Error en editar_gasto.php: respuesta inesperada de sp_editar_gasto para id_gasto {$id_gasto}.");

if ((bool)$fila['success'] === false) {
    responderError($fila['message'] ?? "No se pudo editar el gasto.", 400);
}

// Respuesta exitosa
responderExito($fila['message'] ?? "Gasto actualizado correctamente.", [
    "id_gasto" => (string)$id_gasto,
    "foto_ticket" => (string)$nombre_foto_final
]);
