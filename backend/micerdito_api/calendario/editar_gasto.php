<?php

/**
 * API: Editar Gasto
 * Actualiza los detalles de un gasto existente (Título, Importe, Descripción).
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
    "message" => "Error desconocido"
);

// Recogida de datos enviados desde la App.
$id_gasto    = trim($_POST['id_gasto'] ?? '');
$titulo      = trim($_POST['titulo'] ?? '');
$importe     = trim($_POST['importe'] ?? '');
$descripcion = trim($_POST['descripcion'] ?? '');

/**
 * GESTIÓN DE LA FOTO:
 * 'foto_ticket' contiene el nombre del archivo que ya existe en la BBDD.
 * Por defecto, asumimos que se mantiene la foto actual.
 */
$nombre_foto_final = trim($_POST['foto_ticket'] ?? '');

// Si el usuario ha seleccionado una imagen nueva en la App, procesamos la subida.
if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {
    $directorio_subida = "../uploads/tickets/";
    if (!file_exists($directorio_subida)) {
        mkdir($directorio_subida, 0777, true);
    }

    $extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
    // Generamos un nombre nuevo para la imagen editada.
    $nuevo_nombre = "TK_EDIT_" . $id_gasto . "_" . time() . "." . $extension;
    $ruta_final = $directorio_subida . $nuevo_nombre;

    if (move_uploaded_file($_FILES['foto']['tmp_name'], $ruta_final)) {
        // Si la subida es exitosa, actualizamos el nombre que enviaremos al SP.
        $nombre_foto_final = $nuevo_nombre;
    }
}

/**
 * Validación de seguridad inicial:
 */
if (empty($id_gasto) || empty($titulo) || $importe === '') {
    $response["message"] = "Faltan parámetros obligatorios para editar.";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 */
$sql = "CALL sp_editar_gasto(?, ?, ?, ?, ?)";

if ($stmt = $conexion->prepare($sql)) {
    
    // Vinculamos parámetros: pasamos el nombre_foto_final (sea el viejo o el recién subido).
    $stmt->bind_param("ssdss", $id_gasto, $titulo, $importe, $descripcion, $nombre_foto_final);

    if ($stmt->execute()) {
        $result = $stmt->get_result();
        if ($fila = $result->fetch_assoc()) {
            $response["success"] = (bool)$fila['success'];
            $response["message"] = $fila['message'];
        }
        $result->free();
    } else {
        $response["message"] = "Error al ejecutar la edición del gasto.";
    }

    while ($conexion->next_result()) {
        if ($res = $conexion->store_result()) {
            $res->free();
        }
    }
    $stmt->close();

} else {
    $response["message"] = "Error al preparar la edición en el servidor.";
}

echo json_encode($response);
$conexion->close();
exit;