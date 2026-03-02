<?php

/**
 * API: Insertar Gasto
 * Gestiona la insercción de gastos y la validación de los datos.
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

// Aseguramos comunicación en UTF8
$conexion->set_charset("utf8mb4");

$response = array("success" => false, "message" => "Error desconocido");

// Recogida de datos enviados desde la App.
$id_usuario   = trim($_POST['id_usuario'] ?? '');
$id_categoria = trim($_POST['id_categoria'] ?? '');
$titulo       = trim($_POST['titulo'] ?? '');
$importe      = trim($_POST['importe'] ?? '');
$fecha_gasto  = trim($_POST['fecha_gasto'] ?? '');
$descripcion  = trim($_POST['descripcion'] ?? '');
$nombre_foto = null;

// --- LÓGICA DE SUBIDA DE IMAGEN ---
if (isset($_FILES['foto']) && $_FILES['foto']['error'] === UPLOAD_ERR_OK) {
    $directorio_subida = "../uploads/tickets/"; // Asegúrate de crear esta carpeta con permisos 777
    if (!file_exists($directorio_subida)) {
        mkdir($directorio_subida, 0777, true);
    }

    $extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
    // Creamos un nombre único: TK_IDUSUARIO_TIMESTAMP.jpg
    $nombre_foto = "TK_" . $id_usuario . "_" . time() . "." . $extension;
    $ruta_final = $directorio_subida . $nombre_foto;

    if (!move_uploaded_file($_FILES['foto']['tmp_name'], $ruta_final)) {
        $nombre_foto = null; // Si falla la subida, guardamos null para no romper el registro
    }
}

/**
 * Validación de seguridad inicial:
 * Verificamos que el id del usuario, el id de la categoria, el titulo, el importe y la fecha de gasto no lleguen vacíos antes de procesar la petición.
 */
if (empty($id_usuario) || empty($id_categoria) || empty($titulo) || empty($importe) || empty($fecha_gasto)) {
    $response["message"] = "Faltan campos obligatorios para registrar el gasto.";
    echo json_encode($response);
    exit;
}

/**
 * Preparación de la primera consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_insertar_gasto' definido en MySQL.
 */
if ($stmt = $conexion->prepare("CALL sp_insertar_gasto(?, ?, ?, ?, ?, ?, ?)")) {

    // Vinculamos los parámetro como String ("s") y Decimal ("d").
    $stmt->bind_param("sssdsss", $id_usuario, $id_categoria, $titulo, $importe, $fecha_gasto, $descripcion, $nombre_foto);
    // Ejecutamos la sentencia en el servidor de BBDD.
    if ($stmt->execute()) {
        $res = $stmt->get_result();

        // Verificamos si existe el usuario.
        if ($res && $res->num_rows > 0) {
            $fila = $res->fetch_assoc();

            // Si el SP devuelve el UUID generado.
            $response["success"] = true;
            $response["message"] = "Gasto registrado correctamente.";
            $response["id_gasto"] = $fila['id_gasto'];
        } else {
            // Si el SP no devuelve el ID pero no dio error.
            $response["success"] = true;
            $response["message"] = "Gasto registrado correctamente.";
        }

        if ($res) $res->free();
    } else {
        $response["message"] = "Error al ejecutar el registro del gasto.";
    }

    // Limpieza de resultados para evitar bloqueos en futuras consultas.
    while ($conexion->next_result()) {
        $conexion->store_result();
    }
    $stmt->close();
} else {
    $response["message"] = "Error al preparar la consulta en el servidor.";
}

// Aseguramos que el JSON sea lo último que se envíe.
echo json_encode($response);
$conexion->close();
exit;
