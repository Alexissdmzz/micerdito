<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

$id_usuario = $_POST['id_usuario'] ?? '';
$limite = $_POST['limite'] ?? '';

if(empty($id_usuario) || empty($limite)) {
    echo json_encode(["success" => false, "message" => "Faltan datos"]);
    exit;
}

if ($sentencia = $conexion->prepare("CALL sp_guardar_limite(?, ?)")) {
    $sentencia->bind_param("sd", $id_usuario, $limite); // "s" para string, "d" para decimal/double
    
    if ($sentencia->execute()) {
        echo json_encode(["success" => true, "message" => "Límite actualizado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar"]);
    }
    $sentencia->close();
}
$conexion->close();