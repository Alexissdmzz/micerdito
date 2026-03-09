<?php

/**
 * API: Obtener Resumen Calendario
 * Recupera la fecha de registro, días con actividad y datos para el gráfico mensual.
 */

/**
 * CONTROL DE BUFFER Y ERRORES:
 * ob_get_level: Comprueba si hay contenido almacenado en el búfer de salida.
 * ob_end_clean: Borra cualquier espacio en blanco, aviso (Notice) o eco previo.
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
    "fecha_registro" => null,
    "dias_con_gastos" => array(),
    "resumen_grafico" => array()
);

// Recogida de datos enviados desde la App (Usamos GET para consultas).
$id_usuario = trim($_GET['id_usuario'] ?? '');
$mes        = trim($_GET['mes'] ?? '');
$anio       = trim($_GET['anio'] ?? '');

/**
 * Validación de seguridad inicial:
 * Verificamos que los parámetros necesarios para filtrar el calendario no estén vacíos.
 */
if (empty($id_usuario) || empty($mes) || empty($anio)) {
    $response["message"] = "Faltan parámetros para consultar el calendario.";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * El procedimiento 'sp_obtener_datos_calendario' devuelve 3 SELECTs diferentes.
 * Usamos 'multi_query' o una ejecución directa para procesar múltiples resultsets.
 */
$sql = "CALL sp_obtener_datos_calendario(?, ?, ?)";

if ($stmt = $conexion->prepare($sql)) {
    
    // Vinculamos parámetros: id_usuario (s), mes (i), anio (i).
    $stmt->bind_param("sii", $id_usuario, $mes, $anio);

    if ($stmt->execute()) {
        
        // --- 1. RESULTADO: Fecha de Registro ---
        $res1 = $stmt->get_result();
        if ($fila = $res1->fetch_assoc()) {
            $response["fecha_registro"] = $fila['fecha_registro_usuario'];
        }
        $res1->free();

        // --- 2. RESULTADO: Días con Gastos ---
        // Avanzamos al siguiente set de resultados del SP
        if ($stmt->next_result()) {
            $res2 = $stmt->get_result();
            while ($fila = $res2->fetch_assoc()) {
                $response["dias_con_gastos"][] = (int)$fila['dia'];
            }
            $res2->free();
        }

        // --- 3. RESULTADO: Datos del Gráfico ---
        if ($stmt->next_result()) {
            $res3 = $stmt->get_result();
            while ($fila = $res3->fetch_assoc()) {
                $response["resumen_grafico"][] = array(
                    "nombre" => $fila['nombre_categoria'],
                    "color"  => $fila['color_categoria'],
                    "total"  => (float)$fila['total']
                );
            }
            $res3->free();
        }

        $response["success"] = true;
        $response["message"] = "Datos recuperados correctamente.";

    } else {
        $response["message"] = "Error al ejecutar la consulta del calendario.";
    }

    // Limpieza final de resultados para liberar la conexión.
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