<?php

/**
 * Utilidades comunes para devolver respuestas JSON consistentes en la API.
 */

function responderExito(string $message, array $extra = [], int $statusCode = 200): void
{
    http_response_code($statusCode);
    echo json_encode(array_merge([
        "success" => true,
        "message" => $message
    ], $extra), JSON_UNESCAPED_UNICODE);
    exit;
}

function responderError(string $message, int $statusCode = 400, array $extra = []): void
{
    http_response_code($statusCode);
    echo json_encode(array_merge([
        "success" => false,
        "message" => $message
    ], $extra), JSON_UNESCAPED_UNICODE);
    exit;
}
