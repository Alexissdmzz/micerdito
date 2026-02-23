<?php

/**
 * API - Eliminación de Usuario
 * Gestiona la baja definitiva de un perfil y sus datos asociados.
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
$id_usuario = $_POST["id_usuario"] ?? '';

/**
 * Validación de seguridad inicial:
 * Verificamos que el ID no llegue vacío antes de procesar la petición.
 */
if (empty($id_usuario)) {
    echo json_encode([
        "success" => false,
        "message" => "Identificador de usuario no proporcionado."
    ]);
    exit;
}

/**
 * Preparación de la consulta mediante SP:
 * El uso de 'prepare' evita ataques de Inyección SQL.
 * Se llama al procedimiento 'sp_eliminar_usuario' definido en MySQL.
 */
if ($sentencia = $conexion->prepare("CALL sp_eliminar_usuario(?)")) {

    // Vinculamos el parámetro como String ("s").
    $sentencia->bind_param("s", $id_usuario);

    // Ejecutamos la sentencia en el servidor de BBDD.
    if ($sentencia->execute()) {

        /**
         * Verificación de filas afectadas:
         * Si affected_rows > 0, significa que el usuario existía y fue borrado.
         */
        if ($sentencia->affected_rows > 0) {
            echo json_encode([
                "success" => true,
                "message" => "Cuenta y datos asociados eliminados correctamente.",
                "id_usuario" => $id_usuario
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "No se encontró el registro o el usuario ya fue borrado previamente."
            ]);
        }
    } else {
        // Error durante la ejecución del procedimiento.
        echo json_encode([
            "success" => false,
            "message" => "Error crítico al ejecutar la operación en la base de datos."
        ]);
    }

    // Cerramos la sentencia para liberar recursos del servidor.
    $sentencia->close();
} else {
    // Error en la preparación.
    echo json_encode([
        "success" => false,
        "message" => "Error interno: No se pudo preparar la consulta."
    ]);
}

//Cerramos la conexión.
$conexion->close();
