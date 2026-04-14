<?php

require_once __DIR__ . '/respuesta.php';

/**
 * Guarda una imagen de ticket de forma segura
 * - Valida tipo MIME real
 * - Limita tamaño
 * - Genera nombre único
 */
function guardarImagenTicket(array $file, string $prefijoNombre): ?string
{
    // Validación básica del archivo
    if (!isset($file['error']) || is_array($file['error'])) {
        responderError("Archivo de imagen no válido.", 400);
    }

    if ($file['error'] !== UPLOAD_ERR_OK) {
        responderError("Error al subir la imagen.", 400);
    }

    // Tamaño máximo: 5MB
    $tamanoMaximo = 5 * 1024 * 1024;
    if (($file['size'] ?? 0) <= 0 || $file['size'] > $tamanoMaximo) {
        responderError("La imagen supera el tamaño permitido (5 MB).", 400);
    }

    // Tipos MIME permitidos
    $tiposPermitidos = [
        'image/jpeg' => 'jpg',
        'image/png'  => 'png',
        'image/webp' => 'webp'
    ];

    // Detectar tipo real del archivo
    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mimeReal = $finfo->file($file['tmp_name']);

    if (!isset($tiposPermitidos[$mimeReal])) {
        responderError("Formato de imagen no permitido. Usa JPG, PNG o WEBP.", 400);
    }

    $extension = $tiposPermitidos[$mimeReal];

    // Directorio de subida
    $directorioSubida = __DIR__ . '/../uploads/tickets/';

    if (!is_dir($directorioSubida)) {
        if (!mkdir($directorioSubida, 0755, true) && !is_dir($directorioSubida)) {
            responderError("No se pudo crear el directorio de subida.", 500);
        }
    }

    // Nombre único seguro
    $nombreArchivo = $prefijoNombre . '_' . bin2hex(random_bytes(8)) . '.' . $extension;
    $rutaFinal = $directorioSubida . $nombreArchivo;

    // Verificación extra de seguridad
    if (!is_uploaded_file($file['tmp_name'])) {
        responderError("El archivo subido no es válido.", 400);
    }

    // Mover archivo
    if (!move_uploaded_file($file['tmp_name'], $rutaFinal)) {
        responderError("No se pudo guardar la imagen en el servidor.", 500);
    }

    return $nombreArchivo;
}
