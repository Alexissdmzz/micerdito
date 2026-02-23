<?php

/**
 * API: Obtener Movimientos
 * Gestiona la obtención de los últimos movimientos del usuario.
 */

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Recogida de datos enviados desde la App.
$id_usuario = $_POST['id_usuario'] ?? '';

/**
 * Validación de seguridad inicial:
 * Verificamos que el id del usuario no llegue vacío antes de procesar la petición.
 */
if (empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "ID vacío"]);
    exit;
}

/**
 * Preparación de la primera consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_obtener_movimientos' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_obtener_movimientos(?)")) {
    // Vinculamos el parámetro como String ("s").
    $sentencia->bind_param("s", $id_usuario);
    // Ejecutamos la sentencia en el servidor de BBDD.
    $sentencia->execute();
    $resultado = $sentencia->get_result();

    $movimientos = [];
    while ($fila = $resultado->fetch_assoc()) {
        $movimientos[] = $fila;
    }

    // Si es exitoso, la almacena en el array.
    echo json_encode([
        "success" => true,
        "gastos_recientes" => $movimientos
    ]);

    $sentencia->close();
}
$conexion->close();
