<?php
ob_start();
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
require_once '../conexion/conexion.php';

// 2. Captura de datos desde Android
$nombre_usuario = $_POST['nombre_usuario'] ?? '';
$correo         = strtolower(trim($_POST['correo'] ?? '')); // Normalización inmediata
$pwd            = $_POST['pwd'] ?? '';
$repeat_pwd     = $_POST['repeat_pwd'] ?? '';
$id_pregunta    = $_POST['id_pregunta'] ?? '';
$respuesta      = $_POST['respuesta_seguridad'] ?? '';

$response = array();

// 3. Validación de campos obligatorios
// Nota: Usamos strlen para id_pregunta por si el ID es '0'
if (!empty($nombre_usuario) && !empty($correo) && !empty($pwd) && !empty($repeat_pwd) && strlen($id_pregunta) > 0 && !empty($respuesta)) {

    // Validaciones extra de seguridad en el servidor (Capa 2 de defensa)
    if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        $response['success'] = false;
        $response['message'] = "El correo no tiene un formato válido.";
    } 
    elseif ($pwd !== $repeat_pwd) {
        $response['success'] = false;
        $response['message'] = "Las contraseñas no coinciden.";
    } 
    else {
        // 4. Preparación de seguridad (Hashing)
        $password_hash = password_hash($pwd, PASSWORD_DEFAULT);
        // La respuesta de seguridad también se guarda en hash por privacidad
        $respuesta_hash = password_hash(strtolower(trim($respuesta)), PASSWORD_DEFAULT);

        // 5. Llamada al Stored Procedure
        try {
            if ($stmt = $conexion->prepare("CALL sp_registro(?, ?, ?, ?, ?)")) {
                $id_int = (int)$id_pregunta;
                $stmt->bind_param("sssis", $nombre_usuario, $correo, $password_hash, $id_int, $respuesta_hash);
                
                if ($stmt->execute()) {
                    $result = $stmt->get_result();
                    $datos = $result->fetch_assoc();

                    // IMPORTANTE: Limpiar resultados de MySQL para evitar errores de conexión
                    while($conexion->next_result()) { $conexion->store_result(); }

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

if (ob_get_length()) ob_clean();
echo json_encode($response);
exit;