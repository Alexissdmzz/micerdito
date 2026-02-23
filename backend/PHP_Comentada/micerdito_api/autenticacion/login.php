<?php

/**
 * API: Autenticación de Usuarios
 * Valida credenciales, gestiona intentos fallidos y controla el bloqueo de cuentas.
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
$correo = $_POST['correo'] ?? '';
$passInput = $_POST['pwd'] ?? '';
$response = array();

/**
 * Validación de seguridad inicial:
 * Verificamos que el correo y la pwd no lleguen vacíos antes de procesar la petición.
 */
if (!empty($correo) && !empty($passInput)) {

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
     * Preparación de la primera consulta mediante SP:
     * El uso de 'prepare' evita ataques de Inyección SQL.
     * Se llama al procedimiento 'sp_login' definido en MySQL.
     */
    if ($sentencia = $conexion->prepare("CALL sp_login(?)")) {
        // Vinculamos el parámetro como String ("s").
        $sentencia->bind_param("s", $correo);

        // Ejecutamos la sentencia en el servidor de BBDD.
        $sentencia->execute();

        $resultado = $sentencia->get_result();

        if ($usuario = $resultado->fetch_assoc()) {

            /**
             * Validación de seguridad (Bloqueo):
             * Verificamos si la cuenta está bloqueada.
             */
            if ($usuario['esta_bloqueado'] == 1) {
                $response['success'] = false;
                $response['message'] = "Cuenta bloqueada. Inténtalo en 15 min.";
                $response['user'] = null;
            } else {
                // Cerramos resultados previos para permitir nuevas llamadas a SP.
                $sentencia->close();
                while ($conexion->next_result()) {
                    $conexion->store_result();
                }

                /**
                 * Validación de seguridad (PWD):
                 * Verificamos que la pwd coincide con la almacenada en la BBDD.
                 */
                if (password_verify($passInput, $usuario['pwd'])) {

                    // Login correcto / Se reinician los intentos
                    if ($stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 1)")) {
                        // Vinculamos el parámetro como String ("s").
                        $stmtStatus->bind_param("s", $correo);
                        // Ejecutamos la sentencia en el servidor de BBDD.
                        $stmtStatus->execute();
                        $stmtStatus->close();
                    }

                    $response['success'] = true;
                    $response['message'] = "Bienvenido " . $usuario['nombre_usuario'];
                    $response['user'] = array(
                        'id' => $usuario['id_usuario'],
                        'username' => $usuario['nombre_usuario'],
                        'email' => $usuario['correo']
                    );
                } else {
                    /**
                     * Preparación de la segunda consulta mediante SP:
                     * El uso de 'prepare' evita ataques de Inyección SQL.
                     * Se llama al procedimiento 'sp_login_intentos(?)' definido en MySQL.
                     */
                    if ($stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 0)")) {
                        $stmtStatus->bind_param("s", $correo);
                        $stmtStatus->execute();
                        $stmtStatus->close();
                    }

                    $intentos_hechos = $usuario['intentos_fallidos'];
                    $intentos_restantes = 2 - $intentos_hechos;

                    $response['success'] = false;
                    $response['message'] = ($intentos_restantes > 0)
                        ? "Password incorrecta. Quedan $intentos_restantes intentos."
                        : "Demasiados fallos. Cuenta bloqueada.";
                    $response['user'] = null;
                }
            }
        } else {
            $response['success'] = false;
            $response['message'] = "El usuario no existe";
        }
    }
} else {
    $response['success'] = false;
    $response['message'] = "Faltan datos";
}

// Aseguramos que el JSON sea lo último que se envíe.
if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;
