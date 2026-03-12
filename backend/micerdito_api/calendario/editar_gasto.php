<?php

/**
 * API: Editar Gasto
 * Actualiza los detalles de un gasto existente (Título, Importe, Descripción).
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

// Recogida de datos enviados desde la App (POST para edición).
$id_gasto    = trim($_POST['id_gasto'] ?? '');
$titulo      = trim($_POST['titulo'] ?? '');
$importe     = trim($_POST['importe'] ?? '');
$descripcion = trim($_POST['descripcion'] ?? '');
$foto_ticket = trim($_POST['foto_ticket'] ?? '');

/**
 * Validación de seguridad inicial:
 */
if (empty($id_gasto) || empty($titulo) || $importe === '') {
    $response["message"] = "Faltan parámetros obligatorios para editar.";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 */
$sql = "CALL sp_editar_gasto(?, ?, ?, ?, ?)";

if ($stmt = $conexion->prepare($sql)) {
    
    // Vinculamos parámetros: id_gasto (s), titulo (s), importe (d), descripcion (s), foto_ticket(s).
    $stmt->bind_param("ssdss", $id_gasto, $titulo, $importe, $descripcion, $foto_ticket);

    if ($stmt->execute()) {
        
        $result = $stmt->get_result();
        
        if ($fila = $result->fetch_assoc()) {
            // El SP devuelve columnas 'success' y 'message'
            $response["success"] = (bool)$fila['success'];
            $response["message"] = $fila['message'];
        }
        $result->free();

    } else {
        $response["message"] = "Error al ejecutar la edición del gasto.";
    }

    // Limpieza de resultados para liberar la conexión.
    while ($conexion->next_result()) {
        if ($res = $conexion->store_result()) {
            $res->free();
        }
    }
    $stmt->close();

} else {
    $response["message"] = "Error al preparar la edición en el servidor.";
}

echo json_encode($response);
$conexion->close();
exit;