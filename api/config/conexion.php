<?php

$server = "localhost";
$user = "root";
$password = "";
$database = "micerdito_db";

$conexion = new mysqli($server, $user, $password, $database);

if ($conexion->connect_errno) {
    // Es mejor no usar die() con texto en una API, pero para desarrollo está bien.
    die("Conexión fallida: " . $conexion->connect_errno);
}

// HEMOS QUITADO EL ELSE Y EL ECHO "CONECTADO"
// Si llega aquí, la conexión es exitosa y el archivo no dirá nada.

?>