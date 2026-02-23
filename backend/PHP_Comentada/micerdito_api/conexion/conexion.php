<?php

/**
 * API: Conexión
 * Conecta la APP con MYSQL usando PHP como puente
 */

// Variables de conexión
$server = "localhost";
$user = "root";
$password = "";
$database = "micerdito_db";

// Realizamos la conexión
$conexion = new mysqli($server, $user, $password, $database);

// Manejo de error de conexión
if ($conexion->connect_errno) {
    // Es mejor no usar die() con texto en una API, pero para desarrollo está bien.
    die("Conexión fallida: " . $conexion->connect_errno);
}
