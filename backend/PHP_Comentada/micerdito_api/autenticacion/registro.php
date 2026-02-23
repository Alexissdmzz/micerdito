<?php

/**
 * API: Registro
 * Valida credenciales, gestiona intentos fallidos y da de alta al usuario.
 */

/**
 * CONTROL DE BUFFER Y ERRORES:
 * ob_get_level: Comprueba si hay contenido almacenado en el búfer de salida.
 * ob_end_clean: Borra cualquier espacio en blanco, aviso (Notice) o eco previo.
 * Esto garantiza que el ÚNICO resultado que reciba Android sea el JSON limpio.
 * Sin esto, un espacio en blanco antes de "<?php" rompería el parseo en Kotlin/Retrofit.
 */
ob_start();
error_reporting(0);
ini_set('display_errors', 0);

// Comunicación JSON en UTF-8.
header('Content-Type: application/json; charset=utf-8');

// Clase de conexión a la Base de Datos.
require_once '../conexion/conexion.php';

// Recogida de datos enviados desde la App.
$nombre_usuario = $_POST['nombre_usuario'] ?? '';
$correo         = strtolower(trim($_POST['correo'] ?? ''));
$pwd            = $_POST['pwd'] ?? '';
$repeat_pwd     = $_POST['repeat_pwd'] ?? '';
$id_pregunta    = $_POST['id_pregunta'] ?? '';
$respuesta      = $_POST['respuesta_seguridad'] ?? '';
$response = array();

/**
 * Validación de seguridad inicial:
 * Verificamos que el nombre de usuario, el correo, la repetición de la pwd, la pregunta y su repuesta no lleguen vacíos antes de procesar la petición.
 */
if (!empty($nombre_usuario) && !empty($correo) && !empty($pwd) && !empty($repeat_pwd) && strlen($id_pregunta) > 0 && !empty($respuesta)) {

    /**
     * Validación de seguridad (FILTER_VALIDATE_EMAIL y la discrepancia de las contraseñas):
     * @
     */
    if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        $response['success'] = false;
        $response['message'] = "El correo no tiene un formato válido.";
    } elseif ($pwd !== $repeat_pwd) {
        $response['success'] = false;
        $response['message'] = "Las contraseñas no coinciden.";
    } else {
        // Preparación de seguridad (Hashing)
        $password_hash = password_hash($pwd, PASSWORD_DEFAULT);
        // La respuesta de seguridad también se guarda en hash por privacidad
        $respuesta_hash = password_hash(strtolower(trim($respuesta)), PASSWORD_DEFAULT);

        try {
            /**
             * Preparación de la primera consulta mediante SP:
             * El uso de 'prepare' evita ataques de Inyección SQL.
             * Se llama al procedimiento 'sp_registro' definido en MySQL.
             */
            if ($stmt = $conexion->prepare("CALL sp_registro(?, ?, ?, ?, ?)")) {
                $id_int = (int)$id_pregunta;
                // Vinculamos los parámetros como String ("s") y Int ("i").
                $stmt->bind_param("sssis", $nombre_usuario, $correo, $password_hash, $id_int, $respuesta_hash);

                // Ejecutamos la sentencia en el servidor de BBDD.
                if ($stmt->execute()) {
                    $result = $stmt->get_result();
                    $datos = $result->fetch_assoc();

                    // Limpiamos resultados
                    while ($conexion->next_result()) {
                        $conexion->store_result();
                    }

                    if ($datos && isset($datos['status'])) {
                        $response['success'] = ($datos['status'] === 'success');
                        $response['message'] = $datos['message'];
                    } else {
                        $response['success'] = false;
                        $response['message'] = "Respuesta inesperada del servidor.";
                    }
                } else {
                    $response['success'] = false;
                    $response['message'] = "Error al ejecutar el registro.";
                }
                $stmt->close();
            } else {
                $response['success'] = false;
                $response['message'] = "Error al preparar la consulta.";
            }
        } catch (Exception $e) {
            $response['success'] = false;
            $response['message'] = "Excepción en el servidor: " . $e->getMessage();
        }
    }
} else {
    $response['success'] = false;
    $response['message'] = "Por favor, rellena todos los campos obligatorios.";
}

// Aseguramos que el JSON sea lo último que se envíe.
if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;
