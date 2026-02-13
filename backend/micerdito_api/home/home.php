<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

$id_usuario = $_POST['id_usuario'] ?? '';
$mes = date('n');
$anio = date('Y');

if(empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "No existe ese ID"]);
    exit;
}

if ($sentencia = $conexion->prepare("CALL sp_obtenerdatos(?)")) {
    $sentencia->bind_param("s", $id_usuario);
    $sentencia->execute();
    $resultado = $sentencia->get_result();

    if ($datos = $resultado->fetch_assoc()) {
        $datos['success'] = true;
        echo json_encode($datos);
    } else {
        echo json_encode(["success" => false, "message" => "Usuario no encontrado"]);
    }

    $sentencia->close();

    while($conexion->next_result()) { $conexion->store_result(); }

} else {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

$conexion->close();