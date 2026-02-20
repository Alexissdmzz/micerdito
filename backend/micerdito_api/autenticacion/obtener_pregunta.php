<?php
/**
 * API para obtener la pregunta de seguridad directamente de la tabla usuarios.
 */

if (ob_get_level()) ob_end_clean(); 
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');

require_once '../conexion/conexion.php';

// Aseguramos que la comunicación sea en UTF8
$conexion->set_charset("utf8mb4");

$correo = strtolower(trim($_POST['correo'] ?? ''));
$response = array("success" => false, "message" => "Error desconocido");

if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        $response['success'] = false;
        $response['message'] = "El formato del correo electrónico no es válido.";
        echo json_encode($response);
         exit;   
    }

if (!empty($correo)) {
    // 1. Llamamos al SP que ahora busca en una sola tabla
    if ($stmt = $conexion->prepare("CALL sp_obtener_pregunta(?)")) {
        $stmt->bind_param("s", $correo);
        
        if ($stmt->execute()) {
            $res = $stmt->get_result();
            
            if ($res && $res->num_rows > 0) {
                $fila = $res->fetch_assoc();
                
                // IMPORTANTE: Asegúrate de que el nombre de la columna aquí 
                // coincida con el alias que pusiste en el SELECT del SP (pregunta)
                if (isset($fila['pregunta']) && !empty($fila['pregunta'])) {
                    $response["success"] = true;
                    $response["pregunta"] = (string)$fila['pregunta'];
                } else {
                    $response["message"] = "El usuario no tiene una pregunta registrada.";
                }
            } else {
                $response["message"] = "No existe ninguna cuenta con ese correo.";
            }
            
            if ($res) $res->free();
        } else {
            $response["message"] = "Error de ejecución en la base de datos.";
        }
        
        while($conexion->next_result()) { $conexion->store_result(); }
        $stmt->close();
    } else {
        $response["message"] = "Error al preparar la consulta.";
    }
} else {
    $response["message"] = "Por favor, introduce tu correo electrónico.";
}

echo json_encode($response);
exit;