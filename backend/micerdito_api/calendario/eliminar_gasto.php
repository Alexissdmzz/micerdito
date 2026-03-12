<?php

/**
 * API: Eliminar Gasto
 * Borra permanentemente un registro de gasto mediante su UUID.
 */

/**
 * CONTROL DE BUFFER Y ERRORES:
 */
if (ob_get_level()) ob_end_clean();
error_reporting(0);
ini_set('display_errors', 0);

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Aseguramos comunicación en UTF8
$conexion->set_charset("utf8mb4");

$response = array(
    "success" => false, 
    "message" => "Error desconocido"
);

// Recogida de datos (Usamos POST para acciones de borrado).
$id_gasto = trim($_POST['id_gasto'] ?? '');

/**
 * Validación de seguridad inicial:
 */
if (empty($id_gasto)) {
    $response["message"] = "ID de gasto (UUID) no proporcionado.";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * El procedimiento 'sp_eliminar_gasto' recibe el UUID (VARCHAR 36).
 */
$sql = "CALL sp_eliminar_gasto(?)";

if ($stmt = $conexion->prepare($sql)) {
    
    // Vinculamos parámetro: id_gasto es String ("s") por ser UUID.
    $stmt->bind_param("s", $id_gasto);

    if ($stmt->execute()) {
        
        $result = $stmt->get_result();
        
        if ($fila = $result->fetch_assoc()) {
            // Mapeamos la respuesta del Stored Procedure
            $response["success"] = (bool)$fila['success'];
            $response["message"] = $fila['message'];
        }
        $result->free();

    } else {
        $response["message"] = "Error al ejecutar la eliminación del gasto.";
    }

    // Limpieza de resultados para liberar la conexión (Protocolo multi-resultado).
    while ($conexion->next_result()) {
        if ($res = $conexion->store_result()) {
            $res->free();
        }
    }
    $stmt->close();

} else {
    $response["message"] = "Error al preparar la eliminación en el servidor.";
}

// Aseguramos que el JSON sea lo último que se envíe.
echo json_encode($response);
$conexion->close();
exit;