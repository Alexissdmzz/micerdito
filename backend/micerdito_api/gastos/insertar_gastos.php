<?php
/**
 * API para insertar un nuevo gasto mediante Stored Procedure.
 */

if (ob_get_level()) ob_end_clean(); 
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');

// Ajusta la ruta según tu estructura de carpetas
require_once '../conexion/conexion.php';

// Aseguramos comunicación en UTF8
$conexion->set_charset("utf8mb4");

$response = array("success" => false, "message" => "Error desconocido");

// Recogida de datos del POST
$id_usuario   = trim($_POST['id_usuario'] ?? '');
$id_categoria = trim($_POST['id_categoria'] ?? '');
$titulo       = trim($_POST['titulo'] ?? '');
$importe      = trim($_POST['importe'] ?? '');
$fecha_gasto  = trim($_POST['fecha_gasto'] ?? '');
$descripcion  = trim($_POST['descripcion'] ?? '');

// Validación básica de campos obligatorios
if (empty($id_usuario) || empty($id_categoria) || empty($titulo) || empty($importe) || empty($fecha_gasto)) {
    $response["message"] = "Faltan campos obligatorios para registrar el gasto.";
    echo json_encode($response);
    exit;
}

// 1. Llamamos al SP de inserción
if ($stmt = $conexion->prepare("CALL sp_insertar_gasto(?, ?, ?, ?, ?, ?)")) {
    
    // Vinculación de parámetros (s = string, d = double/decimal)
    // El orden debe coincidir con: p_id_usuario, p_id_categoria, p_titulo, p_importe, p_fecha_gasto, p_descripcion
    $stmt->bind_param("sssdss", $id_usuario, $id_categoria, $titulo, $importe, $fecha_gasto, $descripcion);
    
    if ($stmt->execute()) {
        $res = $stmt->get_result();
        
        if ($res && $res->num_rows > 0) {
            $fila = $res->fetch_assoc();
            
            // Si el SP devuelve el UUID generado (tal como definiste en tu SQL anterior)
            $response["success"] = true;
            $response["message"] = "Gasto registrado correctamente.";
            $response["id_gasto"] = $fila['id_gasto'];
        } else {
            // Si el SP no devuelve el ID pero no dio error, asumimos éxito si la ejecución fue correcta
            $response["success"] = true;
            $response["message"] = "Gasto registrado correctamente.";
        }
        
        if ($res) $res->free();
    } else {
        $response["message"] = "Error al ejecutar el registro del gasto.";
    }
    
    // Limpieza de resultados para evitar bloqueos en futuras consultas
    while($conexion->next_result()) { $conexion->store_result(); }
    $stmt->close();
} else {
    $response["message"] = "Error al preparar la consulta en el servidor.";
}

echo json_encode($response);
$conexion->close();
exit;