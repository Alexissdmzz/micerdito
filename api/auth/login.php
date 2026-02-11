<?php
ob_start();
error_reporting(0);
ini_set('display_errors', 0);

include '../config/conexion.php';
header('Content-Type: application/json; charset=utf-8');

// Ajustado a los nombres de tu nueva tabla
$correo = $_POST['CORREO'] ?? '';
$contraseña = $_POST['CONTRASEÑA'] ?? '';
$response = array();

if (!empty($correo) && !empty($contraseña)) {
    // 1. Consultamos los datos del usuario y su estado de bloqueo
    if ($sentencia = $conexion->prepare("CALL sp_login(?)")) {
        $sentencia->bind_param("s", $correo);
        $sentencia->execute();
        $resultado = $sentencia->get_result();

        if ($usuario = $resultado->fetch_assoc()) {
            
            // 2. ¿La cuenta está bloqueada temporalmente?
            if ($usuario['esta_bloqueado'] == 1) {
                $response['success'] = false;
                $response['message'] = "Cuenta bloqueada por seguridad. Inténtalo de nuevo en 15 minutos.";
                $response['user'] = null;
            } else {
                // Cerramos este resultado para poder llamar al siguiente SP después
                $sentencia->close();
                while($conexion->next_result()) { $conexion->store_result(); }

                // 3. Verificamos la contraseña (usando el nombre de tu columna CONTRASEÑA)
                if (password_verify($contraseña, $usuario['CONTRASEÑA'])) {
                    // ÉXITO: Reseteamos intentos en la BBDD
                    if ($stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 1)")) {
                        $stmtStatus->bind_param("s", $correo);
                        $stmtStatus->execute();
                        $stmtStatus->close();
                    }

                    $response['success'] = true;
                    $response['message'] = "Bienvenido " . $usuario['NOMBRE_USUARIO'];
                    $response['user'] = array(
                        'id' => $usuario['ID_USUARIO'],
                        'username' => $usuario['NOMBRE_USUARIO'],
                        'email' => $usuario['CORREO']
                    );
                } else {
                    // FALLO: Sumamos un intento fallido en la BBDD
                    if ($stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 0)")) {
                        $stmtStatus->bind_param("s", $correo);
                        $stmtStatus->execute();
                        $stmtStatus->close();
                    }

                    $intentos_restantes = 2 - $usuario['INTENTOS_FALLIDOS'];
                    $msg = ($intentos_restantes > 0) 
                           ? "Contraseña incorrecta. Te quedan $intentos_restantes intentos." 
                           : "Has agotado los intentos. Cuenta bloqueada por 15 min.";

                    $response['success'] = false;
                    $response['message'] = $msg;
                    $response['user'] = null;
                }
            }
        } else {
            $response['success'] = false;
            $response['message'] = "El usuario no existe";
            $response['user'] = null;
            $sentencia->close();
        }
        while($conexion->next_result()) { $conexion->store_result(); }
    } else {
        $response['success'] = false;
        $response['message'] = "Error en el sistema de login";
    }
} else {
    $response['success'] = false;
    $response['message'] = "Faltan datos por enviar";
}

if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;