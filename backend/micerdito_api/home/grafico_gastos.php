<?php

/**
 * API: Obtener Gastos para Gráfico
 * Recupera el desglose de gastos por categoría del mes actual para el Dashboard.
 */

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Recogida de datos. En este caso usamos GET para consultas de lectura.
$id_usuario = $_GET['id_usuario'] ?? '';

/**
 * Validación de seguridad inicial:
 * Verificamos que el id del usuario no llegue vacío antes de consultar.
 */
if (empty($id_usuario)) {
    echo json_encode(["success" => false, "message" => "ID de usuario no proporcionado"]);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * Se llama al procedimiento 'sp_obtener_gastos_grafico' para obtener el sumatorio.
 */
if ($sentencia = $conexion->prepare("CALL sp_obtener_gastos_grafico(?)")) {
    
    // Vinculamos el parámetro como String/Integer ("s").
    $sentencia->bind_param("s", $id_usuario);

    // Ejecutamos la sentencia.
    if ($sentencia->execute()) {
        $resultado = $sentencia->get_result();
        $datos = [];

        // Recorremos los resultados para guardarlos en un array.
        while ($fila = $resultado->fetch_assoc()) {
            $datos[] = [
                "nombre" => $fila['nombre'],
                "totalGasto" => (float)$fila['totalGasto'],
                "color" => $fila['color']
            ];
        }

        // Devolvemos el éxito con la lista de datos para el PieChart.
        echo json_encode([
            "success" => true,
            "datos" => $datos
        ]);

    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar el procedimiento"]);
    }
    
    $sentencia->close();
}

$conexion->close();