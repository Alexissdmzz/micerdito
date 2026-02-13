<?php
ob_start();
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

// Usamos nombres limpios para recibir desde Android
$correo = $_POST['correo'] ?? '';
$passInput = $_POST['pwd'] ?? ''; // Lo que viene del móvil
$response = array();

if (!empty($correo) && !empty($passInput)) {

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response['success'] = false;
        $response['message'] = "El formato del correo electrónico no es válido.";
        echo json_encode($response);
         exit;   
    }

    // 1. Llamamos al procedimiento (asegúrate de que el SP sp_login use los nuevos nombres)
    if ($sentencia = $conexion->prepare("CALL sp_login(?)")) {
        $sentencia->bind_param("s", $correo);
        $sentencia->execute();
        $resultado = $sentencia->get_result();

        if ($usuario = $resultado->fetch_assoc()) {
            
            // 2. ¿Account bloqueada?
            if ($usuario['esta_bloqueado'] == 1) {
                $response['success'] = false;
                $response['message'] = "Cuenta bloqueada. Inténtalo en 15 min.";
                $response['user'] = null;
            } else {
                $sentencia->close();
                while($conexion->next_result()) { $conexion->store_result(); }

                // 3. Verificamos la pwd (usando el nuevo nombre de columna de tu tabla)
                if (password_verify($passInput, $usuario['pwd'])) {
                    
                    // TODO OK: Reseteamos los intentos
                    if ($stmtStatus = $conexion->prepare("CALL sp_login_intentos(?, 1)")) {
                        $stmtStatus->bind_param("s", $correo);
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
                    // FAIL: Sumamos intento fallido
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

if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;