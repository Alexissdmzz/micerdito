<?php

/**
 * API: Obtener Datos para Home
 * Gestiona la obtención de datos para la UI de la pantalla Home.
 */

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Devuelve meses y años en Español.
$conexion->query("SET lc_time_names = 'es_ES'");

// Recogida de datos enviados desde la App.
$id_usuario = $_POST['id_usuario'] ?? '';
$mes = date('n');
$anio = date('Y');

/**
 * Validación de seguridad inicial:
 * Verificamos que el id del usuario no llegue vacío antes de procesar la petición.
 */
if (empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "No existe ese ID"]);
    exit;
}

/**
 * Preparación de la primera consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_obtener_datos' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_obtener_datos(?)")) {
    // Vinculamos el parámetro como String ("s").
    $sentencia->bind_param("s", $id_usuario);

    // Ejecutamos la sentencia en el servidor de BBDD.
    $sentencia->execute();
    $resultado = $sentencia->get_result();

    if ($datos = $resultado->fetch_assoc()) {
        $datos['success'] = true;
        echo json_encode($datos);
    } else {
        echo json_encode(["success" => false, "message" => "Usuario no encontrado"]);
    }

    $sentencia->close();

    while ($conexion->next_result()) {
        $conexion->store_result();
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

$conexion->close();
