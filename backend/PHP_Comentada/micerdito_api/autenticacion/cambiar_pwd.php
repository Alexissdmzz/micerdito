<?php

/**
 * API - Recuperación y Cambio de Contraseña
 * Verifica la respuesta de seguridad y actualiza la clave encriptada.
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

// Asegura que caracteres especiales (tildes, ñ) se manejen correctamente.
$conexion->set_charset("utf8mb4");

// Recogida de datos enviados desde la App.
$correo = strtolower(trim($_POST['correo'] ?? ''));
$nueva_pwd_plana = $_POST['nueva_pwd'] ?? '';
$respuesta_usuario = strtolower(trim($_POST['respuesta_seguridad'] ?? ''));
$response = array("success" => false, "message" => "Error desconocido");

/**
 * Validación de seguridad inicial:
 * Verificamos que el correo, la respuesta del usuario y la contraseña nueva no esten vacias.
 */
if (!empty($correo) && !empty($respuesta_usuario) && !empty($nueva_pwd_plana)) {

    /**
     * Validación de seguridad (RegEx):
     * +8 caracteres, Mayúscula, Número y Carácter Especial.
     */
    $pattern = '/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/';

    if (!preg_match($pattern, $nueva_pwd_plana)) {
        $response["message"] = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
        echo json_encode($response);
        exit;
    }

    /**
     * Preparación de la primera consulta mediante SP:
     * El uso de 'prepare' evita ataques de Inyección SQL.
     * Se llama al procedimiento 'sp_recuperar_pregunta' definido en MySQL.
     */
    if ($stmt = $conexion->prepare("CALL sp_recuperar_pregunta(?)")) {
        // Vinculamos el parámetro como String ("s").
        $stmt->bind_param("s", $correo);
        $stmt->execute();
        $res = $stmt->get_result();

        if ($res && $res->num_rows > 0) {
            $fila = $res->fetch_assoc();
            $hash_db = $fila['respuesta_seguridad'];

            // Limpieza de resultados.
            $stmt->close();
            while ($conexion->next_result()) {
                $conexion->store_result();
            }

            // Verificamos la respuesta del usuario e encriptamos su nueva pwd.
            if (password_verify($respuesta_usuario, $hash_db)) {
                $nueva_pwd_hash = password_hash($nueva_pwd_plana, PASSWORD_DEFAULT);

                /**
                 * Preparación de la segunda consulta mediante SP:
                 * El uso de 'prepare' evita ataques de Inyección SQL.
                 * Se llama al procedimiento 'sp_cambiar_pwd' definido en MySQL.
                 */
                if ($update = $conexion->prepare("CALL sp_cambiar_pwd(?, ?)")) {
                    // Vinculamos los parámetros como String ("ss").
                    $update->bind_param("ss", $nueva_pwd_hash, $correo);

                    // Ejecutamos la sentencia en el servidor de BBDD.
                    if ($update->execute()) {
                        $response["success"] = true;
                        $response["message"] = "¡Contraseña actualizada con éxito!";
                    } else {
                        $response["message"] = "Error al actualizar la base de datos.";
                    }
                    $update->close();
                }
            } else {
                $response["message"] = "La respuesta de seguridad es incorrecta.";
            }
        } else {
            $response["message"] = "Usuario no encontrado.";
            $stmt->close();
        }
    }
} else {
    $response["message"] = "Faltan datos obligatorios.";
}

// Envío de respuesta final en formato JSON.
echo json_encode($response);
exit;
