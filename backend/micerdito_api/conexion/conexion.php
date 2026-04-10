<?php

/**
 * API: Conexión
 * Conecta la APP con MYSQL usando PHP como puente
 */

header('Content-Type: application/json; charset=utf-8');

// Variables de conexión
$server = "localhost";
$user = "root";
$password = "";
$database = "micerdito_db";

// Realizamos la conexión
$conexion = new mysqli($server, $user, $password, $database);

// Forzamos UTF-8
$conexion->set_charset("utf8mb4");

// Si falla la conexión, devolvemos JSON controlado
if ($conexion->connect_errno) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error de conexión con la base de datos"
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Forzamos UTF-8
if (!$conexion->set_charset("utf8mb4")) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error interno de configuración de la base de datos"
    ], JSON_UNESCAPED_UNICODE);
    exit;
}
