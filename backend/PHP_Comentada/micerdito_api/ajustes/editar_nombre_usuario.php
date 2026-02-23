<?php

/**
 * API - Editar Nombre de Usuario
 * Gestiona el cambio de nombre de usuario.
 */

/**
 * CONTROL DE BUFFER Y ERRORES:
 * ob_get_level: Comprueba si hay contenido almacenado en el búfer de salida.
 * ob_end_clean: Borra cualquier espacio en blanco, aviso (Notice) o eco previo.
 * Esto garantiza que el ÚNICO resultado que reciba Android sea el JSON limpio.
 * Sin esto, un espacio en blanco antes de "<?php" rompería el parseo en Kotlin/Retrofit.
 */
if (ob_get_level()) ob_end_clean();
error_reporting(0);
ini_set('display_errors', 0);

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Recogida de datos enviados desde la App.
$id_usuario = $_POST["id_usuario"] ?? '';
$nombre_usuario = $_POST['nombre_usuario'] ?? '';

/**
 * Validación de seguridad inicial:
 * Verificamos que el ID y el nombre de usuario no lleguen vacíos antes de procesar la petición.
 */
if (empty($id_usuario) || empty($nombre_usuario)) {
    echo json_encode(["success" => false, "message" => "Faltan datos obligatorios"]);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_editar_nom_usu' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_editar_nom_usu(?, ?)")) {

    // Vinculamos los parámetros como String ("ss").
    $sentencia->bind_param("ss", $id_usuario, $nombre_usuario);

    // Ejecutamos la sentencia en el servidor de BBDD.
    if ($sentencia->execute()) {
        // Verificamos si se ejecutó bien.
        echo json_encode([
            "success" => true,
            "message" => "Nombre actualizado a: " . $nombre_usuario
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al ejecutar el procedimiento"]);
    }

    // Cerramos la sentencia para liberar recursos del servidor.
    $sentencia->close();
} else {
    // Error en la preparación.
    echo json_encode(["success" => false, "message" => "Error al preparar la consulta"]);
}

//Cerramos la conexión.
$conexion->close();
