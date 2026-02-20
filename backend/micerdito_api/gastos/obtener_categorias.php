<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

if ($sentencia = $conexion->prepare("CALL sp_obtener_categorias()")) {
    
    if ($sentencia->execute()) {
        $resultado = $sentencia->get_result();
        $categorias = array();
        
        while ($fila = $resultado->fetch_assoc()) {
            $categorias[] = $fila;
        }
        
        echo json_encode([
            "success" => true,
            "categorias" => $categorias
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar el SP"]);
    }
    
    $sentencia->close();
} else {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

$conexion->close();