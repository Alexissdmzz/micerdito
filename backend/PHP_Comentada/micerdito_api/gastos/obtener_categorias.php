<?php

/**
 * API: Obtener Categorias
 * Gestiona la obtención de las categorías ya creadas en la BBDD.
 */

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

/**
 * Preparación de la primera consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_obtener_categorias' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_obtener_categorias()")) {

    // Ejecutamos la sentencia en el servidor de BBDD.
    if ($sentencia->execute()) {
        $resultado = $sentencia->get_result();
        $categorias = array();

        while ($fila = $resultado->fetch_assoc()) {
            $categorias[] = $fila;
        }

        // Si es exitoso, las almacenamos en el array.
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
