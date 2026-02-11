<?php
ob_start();
error_reporting(0);
ini_set('display_errors', 0);

include '../config/conexion.php';
header('Content-Type: application/json; charset=utf-8');

// Ajustado a los nombres de tu nueva tabla y variables POST
$nombre_usuario = $_POST['NOMBRE_USUARIO'] ?? '';
$correo         = $_POST['CORREO'] ?? '';
$pwd            = $_POST['CONTRASEÑA'] ?? '';
$repeatpwd      = $_POST['REPEAT_CONTRASEÑA'] ?? '';

$response = array();

if (!empty($nombre_usuario) && !empty($correo) && !empty($pwd) && !empty($repeatpwd)) {
    
    // 1. Validar coincidencia
    if ($pwd !== $repeatpwd) {
        $response['success'] = false;
        $response['message'] = "Las contraseñas no coinciden";
    } 
    // 2. Validar patrón de seguridad (Regex)
    else if (!preg_match('/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/', $pwd)) {
        $response['success'] = false;
        $response['message'] = "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, números y un carácter especial.";
    }
    else {
        // 3. Llamar al SP (Asegúrate de que sp_register use los nuevos nombres)
        if ($sentencia = $conexion->prepare("CALL sp_registro(?, ?, ?)")) {
            // Usamos PASSWORD_DEFAULT para generar el Hash seguro
            $password_segura = password_hash($pwd, PASSWORD_DEFAULT);
            
            $sentencia->bind_param("sss", $nombre_usuario, $correo, $password_segura);
            $sentencia->execute();
            $resultado = $sentencia->get_result();
            $datos = $resultado->fetch_assoc();

            if ($datos['status'] === 'success') {
                $response['success'] = true;
                $response['message'] = $datos['message'];
            } else {
                $response['success'] = false;
                $response['message'] = $datos['message'];
            }
            $sentencia->close();
        } else {
            $response['success'] = false;
            $response['message'] = "Error interno del servidor al registrar";
        }
    }
} else {
    $response['success'] = false;
    $response['message'] = "Por favor, rellena todos los campos";
}

if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;