<?php

/**
 * API: Eliminación de Usuario
 * Gestiona la baja definitiva de un perfil y de los datos asociados a la cuenta.
 * Este proceso elimina el registro del usuario de forma permanente.
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
$id_usuario = trim($_POST['id_usuario'] ?? '');

if (empty($id_usuario)) {
    responderError("Identificador de usuario no proporcionado.", 400);
}

if (!preg_match('/^[a-f0-9-]+$/i', $id_usuario)) {
    responderError("Identificador de usuario inválido.", 400);
}

/**
 * Validación de autenticación:
 * Comprobamos que el usuario exista realmente antes de operar.
 */
validarUsuarioExistente($conexion, $id_usuario);

// Preparación de la consulta
$stmt = $conexion->prepare("CALL sp_eliminar_usuario(?)");

if (!$stmt) {
    error_log("Error en borrar_usuario.php al preparar sp_eliminar_usuario: " . $conexion->error);
    responderError("Error interno del servidor", 500);
}

$stmt->bind_param("s", $id_usuario);

// Ejecución
if (!$stmt->execute()) {
    error_log("Error en borrar_usuario.php al ejecutar sp_eliminar_usuario para id_usuario {$id_usuario}: " . $stmt->error);
    $stmt->close();
    responderError("Error interno del servidor", 500);
}

// Verificamos el resultado de la operación
if ($stmt->affected_rows > 0) {
    $stmt->close();

    while ($conexion->next_result()) {
        $conexion->store_result();
    }

    responderExito("Cuenta y datos asociados eliminados correctamente.", [
        "id_usuario" => (string)$id_usuario
    ]);
}

$stmt->close();

// Limpiamos resultados pendientes del procedimiento
while ($conexion->next_result()) {
    $conexion->store_result();
}

responderError("No se encontró el registro o el usuario ya fue borrado previamente.", 404);
