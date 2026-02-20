<?php
if (ob_get_level()) ob_end_clean(); 
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';
$conexion->set_charset("utf8mb4");

$correo = strtolower(trim($_POST['correo'] ?? ''));
$nueva_pwd_plana = $_POST['nueva_pwd'] ?? '';
$respuesta_usuario = strtolower(trim($_POST['respuesta_seguridad'] ?? ''));

$response = array("success" => false, "message" => "Error desconocido");

if (!empty($correo) && !empty($respuesta_usuario) && !empty($nueva_pwd_plana)) {

    // 1. VALIDACIÓN DE FORMATO DE CONTRASEÑA (Regex)
    // ^(?=.*[a-z]) -> Al menos una minúscula
    // (?=.*[A-Z])  -> Al menos una mayúscula
    // (?=.*\d)     -> Al menos un número
    // (?=.*[\W_])  -> Al menos un carácter especial (no alfanumérico)
    // .{8,}        -> Mínimo 8 caracteres
    $pattern = '/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$/';

    if (!preg_match($pattern, $nueva_pwd_plana)) {
        $response["message"] = "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial.";
        echo json_encode($response);
        exit;
    }

    // 2. Buscamos el hash de la respuesta en la DB
    $stmt = $conexion->prepare("SELECT respuesta_seguridad FROM usuarios WHERE correo = ?");
    $stmt->bind_param("s", $correo);
    $stmt->execute();
    $res = $stmt->get_result();
    
    if ($res && $res->num_rows > 0) {
        $fila = $res->fetch_assoc();
        $hash_db = $fila['respuesta_seguridad'];
        
        // 3. Verificamos respuesta (Normalizada a minúsculas como en el registro)
        if (password_verify($respuesta_usuario, $hash_db)) {
            
            // 4. Todo OK -> Encriptamos y actualizamos
            $nueva_pwd_hash = password_hash($nueva_pwd_plana, PASSWORD_DEFAULT);
            
            $update = $conexion->prepare("UPDATE usuarios SET pwd = ? WHERE correo = ?");
            $update->bind_param("ss", $nueva_pwd_hash, $correo);
            
            if ($update->execute()) {
                $response["success"] = true;
                $response["message"] = "¡Contraseña actualizada con éxito!";
            } else {
                $response["message"] = "Error al actualizar la base de datos.";
            }
            $update->close();
        } else {
            $response["message"] = "La respuesta de seguridad es incorrecta.";
        }
    } else {
        $response["message"] = "Usuario no encontrado.";
    }
    $stmt->close();
} else {
    $response["message"] = "Faltan datos obligatorios.";
}

echo json_encode($response);
exit;