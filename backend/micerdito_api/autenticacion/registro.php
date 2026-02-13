<?php
ob_start();
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

// Recogemos los campos básicos
$nombre_usuario = $_POST['nombre_usuario'] ?? '';
$correo         = $_POST['correo'] ?? '';
$pwd            = $_POST['pwd'] ?? '';
$repeatpwd      = $_POST['repeat_pwd'] ?? '';

// NUEVOS CAMPOS: Pregunta y Respuesta
$id_pregunta    = $_POST['id_pregunta'] ?? ''; 
$respuesta      = $_POST['respuesta_seguridad'] ?? '';

$response = array();

// Verificamos que nada venga vacío (incluyendo los nuevos campos)
if (!empty($nombre_usuario) && !empty($correo) && !empty($pwd) && !empty($repeatpwd) && !empty($id_pregunta) && !empty($respuesta)) {

    // Validar formato de correo (Corregido: usamos la variable $correo)
    if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        $response['success'] = false;
        $response['message'] = "El formato del correo electrónico no es válido.";
    } 
    // 1. Validar coincidencia de contraseñas
    else if ($pwd !== $repeatpwd) {
        $response['success'] = false;
        $response['message'] = "Las contraseñas no coinciden";
    } 
    // 2. Validar patrón de seguridad de contraseña (Regex)
    else if (!preg_match('/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/', $pwd)) {
        $response['success'] = false;
        $response['message'] = "La contraseña debe ser más robusta (8 caracteres, mayúsculas, números y símbolos).";
    }
    else {
        // 3. Preparación de datos para la BD
        // Hash para la contraseña
        $password_segura = password_hash($pwd, PASSWORD_DEFAULT);
        
        // Hash para la respuesta de seguridad (limpiamos espacios y pasamos a minúsculas antes)
        $respuesta_limpia = strtolower(trim($respuesta));
        $respuesta_segura = password_hash($respuesta_limpia, PASSWORD_DEFAULT);

        // 4. Llamar al SP (Añadimos los 2 parámetros extra: id_pregunta y respuesta_segura)
        if ($sentencia = $conexion->prepare("CALL sp_registro(?, ?, ?, ?, ?)")) {
            
            // "sss i s" -> string, string, string, integer, string
            $sentencia->bind_param("sssis", $nombre_usuario, $correo, $password_segura, $id_pregunta, $respuesta_segura);
            
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
            $response['message'] = "Error interno del servidor al preparar el registro";
        }
    }
} else {
    $response['success'] = false;
    $response['message'] = "Por favor, rellena todos los campos, incluyendo la pregunta de seguridad";
}

if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;