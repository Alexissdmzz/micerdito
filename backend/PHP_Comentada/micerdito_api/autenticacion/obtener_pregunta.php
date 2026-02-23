<?php

/**
 * API: Obtención de Pregunta de Seguridad
 * Recupera el texto de la pregunta de seguridad vinculada a un correo electrónico.
 * Este es el primer paso del flujo de recuperación de cuenta
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

// Aseguramos que la comunicación sea en UTF8
$conexion->set_charset("utf8mb4");

// Recogida de datos enviados desde la App.
$correo = strtolower(trim($_POST['correo'] ?? ''));
$response = array("success" => false, "message" => "Error desconocido");

/**
 * Validación de seguridad (FILTER_VALIDATE_EMAIL):
 * @
 */
if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    $response['success'] = false;
    $response['message'] = "El formato del correo electrónico no es válido.";
    echo json_encode($response);
    exit;
}

/**
 * Validación de seguridad:
 * Verificamos que el correo no llegue vacío antes de procesar la petición.
 */
if (!empty($correo)) {
    /**
     * Preparación de la primera consulta mediante SP:
     * El uso de 'prepare' evita ataques de Inyección SQL.
     * Se llama al procedimiento 'sp_obtener_pregunta(?)' definido en MySQL.
     */
    if ($stmt = $conexion->prepare("CALL sp_obtener_pregunta(?)")) {
        // Vinculamos el parámetro como String ("s").
        $stmt->bind_param("s", $correo);

        // Ejecutamos la sentencia en el servidor de BBDD.
        if ($stmt->execute()) {
            $res = $stmt->get_result();

            // Verificamos si existe el usuario
            if ($res && $res->num_rows > 0) {
                $fila = $res->fetch_assoc();

                /**
                 * Validación de seguridad:
                 * Verificamos que el campo pregunta no sea nulo o este vacío.
                 */
                if (isset($fila['pregunta']) && !empty($fila['pregunta'])) {
                    $response["success"] = true;
                    $response["pregunta"] = (string)$fila['pregunta'];
                } else {
                    $response["message"] = "El usuario no tiene una pregunta registrada.";
                }
            } else {
                $response["message"] = "No existe ninguna cuenta con ese correo.";
            }

            // Liberación de memoria del resultado
            if ($res) $res->free();
        } else {
            $response["message"] = "Error de ejecución en la base de datos.";
        }

        while ($conexion->next_result()) {
            $conexion->store_result();
        }
        $stmt->close();
    } else {
        $response["message"] = "Error al preparar la consulta.";
    }
} else {
    $response["message"] = "Por favor, introduce tu correo electrónico.";
}

// Envío de la respuesta final en formato JSON.
echo json_encode($response);
exit;
