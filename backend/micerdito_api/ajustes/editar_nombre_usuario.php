<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

$id_usuario = $_POST["id_usuario"] ?? '';
$nombre_usuario = $_POST['nombre_usuario'] ?? ''; // Siempre usa ?? para evitar errores de índice

if(empty($id_usuario) || empty($nombre_usuario)) {
    echo json_encode(["success" => false, "message" => "Faltan datos obligatorios"]);
    exit;
}

// 1. Preparamos con DOS interrogantes (?, ?)
if ($sentencia = $conexion->prepare("CALL sp_editar_nom_usu(?, ?)")) {
    
    // 2. Pasamos DOS strings ("ss") y las DOS variables
    $sentencia->bind_param("ss", $id_usuario, $nombre_usuario);
    
    if ($sentencia->execute()) {
        // 3. En un CALL de UPDATE, simplemente verificamos si se ejecutó bien
        echo json_encode([
            "success" => true, 
            "message" => "Nombre actualizado a: " . $nombre_usuario
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar el procedimiento"]);
    }

    $sentencia->close();
} else {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

$conexion->close();