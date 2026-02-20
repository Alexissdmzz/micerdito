<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

$id_usuario = $_POST["id_usuario"] ?? '';

if(empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "No existe ese ID"]);
    exit;
}

if($sentencia = $conexion->prepare("CALL sp_eliminar_usuario(?)")) {
    $sentencia->bind_param("s", $id_usuario);
    $sentencia->execute();

    if ($sentencia->affected_rows > 0) {
        echo json_encode([
            "success" => true, 
            "message" => "Cuenta eliminada correctamente",
            "id_usuario" => $id_usuario
        ]);
    } else {
        echo json_encode([
            "success" => false, 
            "message" => "No se encontró el usuario o ya fue borrado"
        ]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

$conexion->close();
