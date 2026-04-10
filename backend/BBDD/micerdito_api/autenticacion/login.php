<?php

/**
 * API: Autenticación de Usuarios
 * Valida credenciales, gestiona intentos fallidos y controla el bloqueo de cuentas.
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

// Recogida de datos enviados desde la App
$correo = trim($_POST['correo'] ?? '');
$passInput = $_POST['pwd'] ?? '';

// Validación básica
if (empty($correo) || empty($passInput)) {
    responderError("Faltan datos", 400);
}

// Normalización del correo
$correo = mb_strtolower($correo, 'UTF-8');

// Validación de formato
if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    responderError("El formato del correo electrónico no es válido.", 400);
}

// Consulta principal de login
$sentencia = $conexion->prepare("CALL sp_login(?)");

if (!$sentencia) {
    responderError("Error interno del servidor", 500);
}

$sentencia->bind_param("s", $correo);
$sentencia->execute();

$resultado = $sentencia->get_result();

if (!$resultado) {
    $sentencia->close();
    responderError("Error interno del servidor", 500);
}

$usuario = $resultado->fetch_assoc();

if (!$usuario) {
    $sentencia->close();
    responderError("El usuario no existe", 404);
}

/**
 * Validación de bloqueo
 */
if ((int)$usuario['esta_bloqueado'] === 1) {
    $sentencia->close();
    responderError("Cuenta bloqueada. Inténtalo en 15 min.", 403);
}

// Cerramos resultados previos para permitir nuevas llamadas a procedimientos almacenados
$sentencia->close();
while ($conexion->next_result()) {
    $conexion->store_result();
}

/**
 * Validación de contraseña
 */
if (password_verify($passInput, $usuario['pwd'])) {

    $stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 1)");
    if ($stmtStatus) {
        $stmtStatus->bind_param("s", $correo);
        $stmtStatus->execute();
        $stmtStatus->close();
    }

    responderExito("Bienvenido " . $usuario['nombre_usuario'], [
        'user' => [
            'id' => $usuario['id_usuario'],
            'username' => $usuario['nombre_usuario'],
            'email' => $usuario['correo']
        ]
    ]);
}

// Password incorrecta
$stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 0)");
if ($stmtStatus) {
    $stmtStatus->bind_param("s", $correo);
    $stmtStatus->execute();
    $stmtStatus->close();
}

$intentosHechos = (int)$usuario['intentos_fallidos'];
$intentosRestantes = 2 - $intentosHechos;

$mensaje = ($intentosRestantes > 0)
    ? "Contraseña incorrecta. Quedan $intentosRestantes intentos."
    : "Demasiados fallos. Cuenta bloqueada.";

responderError($mensaje, 401);
