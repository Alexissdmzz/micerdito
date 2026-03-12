<?php

/**
 * API: Obtener Gastos por Día
 * Recupera la lista detallada de gastos para una fecha específica (Año-Mes-Día).
 */

/**
 * CONTROL DE BUFFER Y ERRORES:
 */
if (ob_get_level()) ob_end_clean();
error_reporting(0);
ini_set('display_errors', 0);

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Aseguramos comunicación en UTF8
$conexion->set_charset("utf8mb4");

$response = array(
    "success" => false, 
    "message" => "Error desconocido",
    "data" => array() // Aquí irán los objetos tipo Gasto
);

// Recogida de datos enviados desde la App (Usamos GET para consultas).
$id_usuario = trim($_GET['id_usuario'] ?? '');
$anio       = trim($_GET['anio'] ?? '');
$mes        = trim($_GET['mes'] ?? '');
$dia        = trim($_GET['dia'] ?? '');

/**
 * Validación de seguridad inicial:
 */
if (empty($id_usuario) || empty($anio) || empty($mes) || empty($dia)) {
    $response["message"] = "Faltan parámetros (usuario, año, mes o día).";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * El procedimiento 'sp_obtener_gastos_dia' devuelve los detalles de los gastos.
 */
$sql = "CALL sp_obtener_gastos_dia(?, ?, ?, ?)";

if ($stmt = $conexion->prepare($sql)) {
    
    // Vinculamos parámetros: id_usuario (s), anio (i), mes (i), dia (i).
    $stmt->bind_param("siii", $id_usuario, $anio, $mes, $dia);

    if ($stmt->execute()) {
        
        $result = $stmt->get_result();
        
        while ($fila = $result->fetch_assoc()) {
            // Mapeamos los campos para que coincidan con @SerializedName en Android
            $response["data"][] = array(
                "id_gasto"        => $fila['id_gasto'],
                "titulo"          => $fila['titulo'],
                "importe"         => (float)$fila['importe'],
                "descripcion"     => $fila['descripcion'],
                "fecha_gasto"     => $fila['fecha_gasto'], // Mapea a 'fecha' en Kotlin
                "icono_categoria" => $fila['icono_categoria'], // Mapea a 'icono'
                "color_categoria" => $fila['color_categoria'], // Mapea a 'color'
                "foto_ticket"     => $fila['foto_ticket']
            );
        }
        $result->free();

        $response["success"] = true;
        $response["message"] = count($response["data"]) > 0 ? "Gastos recuperados." : "No hay gastos para este día.";

    } else {
        $response["message"] = "Error al ejecutar la consulta de gastos diarios.";
    }

    // Limpieza de resultados para liberar la conexión (Protocolo multi-resultado).
    while ($conexion->next_result()) {
        if ($res = $conexion->store_result()) {
            $res->free();
        }
    }
    $stmt->close();

} else {
    $response["message"] = "Error al preparar la consulta en el servidor.";
}

// Aseguramos que el JSON sea lo último que se envíe.
echo json_encode($response);
$conexion->close();
exit;