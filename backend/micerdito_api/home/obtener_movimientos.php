<?php
header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

$id_usuario = $_POST['id_usuario'] ?? '';

if(empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "ID vacío"]);
    exit;
}

if ($sentencia = $conexion->prepare("CALL sp_obtener_movimientos(?)")) {
    $sentencia->bind_param("s", $id_usuario);
    $sentencia->execute();
    $resultado = $sentencia->get_result();

    $movimientos = [];
    while ($fila = $resultado->fetch_assoc()) {
        $movimientos[] = $fila;
    }

    echo json_encode([
        "success" => true,
        "gastos_recientes" => $movimientos
    ]);
    
    $sentencia->close();
}
$conexion->close();