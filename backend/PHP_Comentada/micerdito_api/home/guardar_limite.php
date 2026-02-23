<?php

/**
 * API: Crear Limite
 * Gestiona la creación de un límite de gasto mensual.
 */

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Recogida de datos enviados desde la App.
$id_usuario = $_POST['id_usuario'] ?? '';
$limite = $_POST['limite'] ?? '';

/**
 * Validación de seguridad inicial:
 * Verificamos que el id del usuario y el límite no lleguen vacíos antes de procesar la petición.
 */
if (empty($id_usuario) || empty($limite)) {
    echo json_encode(["success" => false, "message" => "Faltan datos"]);
    exit;
}
/**
 * Preparación de la primera consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_guardar_limite' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_guardar_limite(?, ?)")) {
    // Vinculamos los parámetros como String ("s") y Decimal ("d").
    $sentencia->bind_param("sd", $id_usuario, $limite);

    // Ejecutamos la sentencia en el servidor de BBDD.
    if ($sentencia->execute()) {
        echo json_encode(["success" => true, "message" => "Límite actualizado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar"]);
    }
    $sentencia->close();
}
$conexion->close();
