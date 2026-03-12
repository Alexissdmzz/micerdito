-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 12-03-2026 a las 13:25:16
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `micerdito_db`
--

DELIMITER $$
--
-- Procedimientos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cambiar_pwd` (IN `p_correo` VARCHAR(255), IN `p_nueva_pwd_hash` VARCHAR(255))   BEGIN
    -- Cambiamos la contraseña del usuario identificado por su correo
    UPDATE usuarios 
    SET pwd = p_nueva_pwd_hash 
    WHERE correo = p_correo;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_editar_gasto` (IN `p_id_gasto` VARCHAR(36), IN `p_titulo` VARCHAR(100), IN `p_importe` DECIMAL(10,2), IN `p_descripcion` TEXT, IN `p_foto_ticket` VARCHAR(255))   BEGIN
    UPDATE gastos 
    SET titulo = p_titulo, 
        importe = p_importe, 
        descripcion = p_descripcion,
        foto_ticket = p_foto_ticket
    WHERE id_gasto = p_id_gasto;

    IF ROW_COUNT() > 0 THEN
        SELECT 1 AS success, 'Gasto actualizado correctamente' AS message;
    ELSE
        SELECT 0 AS success, 'No se encontró el gasto o no hubo cambios' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_editar_nom_usu` (IN `p_id_usuario` VARCHAR(36), IN `p_nuevo_nombre` VARCHAR(100))   BEGIN
    -- Actualizamos el nombre de usuario basándonos en su UUID
    UPDATE usuarios 
    SET nombre_usuario = p_nuevo_nombre 
    WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_gasto` (IN `p_id_gasto` VARCHAR(36))   BEGIN
    DELETE FROM gastos WHERE id_gasto = p_id_gasto;

    IF ROW_COUNT() > 0 THEN
        SELECT 1 AS success, 'Gasto eliminado correctamente' AS message;
    ELSE
        SELECT 0 AS success, 'Error: El ID de gasto no existe' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_usuario` (IN `p_id_usuario` VARCHAR(36))   BEGIN
    DELETE FROM usuarios 
    WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_guardar_limite` (IN `p_id_usuario` VARCHAR(36), IN `p_limite` DECIMAL(10,2))   BEGIN
    DECLARE v_mes TINYINT;
    DECLARE v_anio SMALLINT;
    DECLARE v_existe INT;

    -- Obtenemos el mes y año actuales automáticamente
    SET v_mes = MONTH(NOW());
    SET v_anio = YEAR(NOW());

    -- Comprobamos si ya existe un registro para este usuario en el mes actual
    SELECT COUNT(*) INTO v_existe 
    FROM presupuesto_mensual 
    WHERE id_usuario = p_id_usuario AND mes = v_mes AND anio = v_anio;

    IF v_existe > 0 THEN
        -- Actualizar el límite existente
        UPDATE presupuesto_mensual 
        SET limite = p_limite 
        WHERE id_usuario = p_id_usuario AND mes = v_mes AND anio = v_anio;
    ELSE
        -- Insertar un nuevo presupuesto con un nuevo UUID
        INSERT INTO presupuesto_mensual (id_presupuesto, id_usuario, limite, mes, anio)
        VALUES (UUID(), p_id_usuario, p_limite, v_mes, v_anio);
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertar_gasto` (IN `p_id_usuario` VARCHAR(36), IN `p_id_categoria` VARCHAR(36), IN `p_titulo` VARCHAR(100), IN `p_importe` DECIMAL(10,2), IN `p_fecha_gasto` DATETIME, IN `p_descripcion` TEXT, IN `p_foto_ticket` VARCHAR(255))   BEGIN
    SET @v_id_gasto = UUID();

    INSERT INTO gastos (
        id_gasto, 
        id_usuario, 
        id_categoria, 
        titulo, 
        importe, 
        fecha_gasto, 
        descripcion,
        foto_ticket
    )
    VALUES (
        @v_id_gasto, 
        p_id_usuario, 
        p_id_categoria, 
        p_titulo, 
        p_importe, 
        p_fecha_gasto, 
        p_descripcion,
        p_foto_ticket
    );

    -- Devolvemos el ID generado
    SELECT @v_id_gasto AS id_gasto;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login` (IN `p_correo` VARCHAR(255))   BEGIN
    SELECT id_usuario, nombre_usuario, correo, pwd, intentos_fallidos, 
           IF(fecha_bloqueo > NOW(), 1, 0) AS esta_bloqueado
    FROM usuarios
    WHERE correo = p_correo;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login_intentos` (IN `p_correo` VARCHAR(255), IN `p_correcto` BOOLEAN)   BEGIN
   IF p_correcto THEN
        -- Si entró bien, reseteamos intentos y quitamos bloqueo
        UPDATE usuarios 
        SET intentos_fallidos = 0, fecha_bloqueo = NULL 
        WHERE correo = p_correo;
    ELSE
        -- Si falló, sumamos uno al contador actual
        UPDATE usuarios 
        SET intentos_fallidos = intentos_fallidos + 1 
        WHERE correo = p_correo;

        -- Si llega a 3 o más, bloqueamos por 15 minutos desde este momento
        UPDATE usuarios 
        SET fecha_bloqueo = DATE_ADD(NOW(), INTERVAL 15 MINUTE) 
        WHERE correo = p_correo AND intentos_fallidos >= 3;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_categorias` ()   BEGIN
    -- Obtenemos todas las categorías disponibles para el selector de gastos
    -- Ordenamos por nombre para que al usuario le sea fácil encontrarlas
    SELECT
    	id_categoria,
        nombre_categoria AS nombre,
        icono_categoria as icono,
        color_categoria as color
    FROM categoria;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_datos` (IN `p_id_usuario` VARCHAR(36))   BEGIN
    DECLARE v_mes TINYINT;
    DECLARE v_anio SMALLINT;
    
    -- Detectamos el mes y año actuales
    SET v_mes = MONTH(NOW());
    SET v_anio = YEAR(NOW());

    SELECT 
        u.nombre_usuario,
        -- Sumamos los gastos del mes actual para este usuario
        IFNULL((SELECT SUM(importe) 
                FROM gastos 
                WHERE id_usuario = p_id_usuario 
                  AND MONTH(fecha_gasto) = v_mes 
                  AND YEAR(fecha_gasto) = v_anio), 0) AS total_dinerogastado,
        -- Obtenemos el límite del presupuesto mensual
        IFNULL((SELECT limite 
                FROM presupuesto_mensual 
                WHERE id_usuario = p_id_usuario 
                  AND mes = v_mes 
                  AND anio = v_anio), 0) AS limite_mes,
        -- Formateamos el nombre del mes para el TextView
        DATE_FORMAT(NOW(), '%M') AS mes_actual
    FROM usuarios u
    WHERE u.id_usuario = p_id_usuario;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_datos_calendario` (IN `p_id_usuario` VARCHAR(36), IN `p_mes` INT, IN `p_anio` INT)   BEGIN
    -- CONSULTA 1: Fecha de registro del usuario
    SELECT fecha_registro_usuario 
    FROM usuarios 
    WHERE id_usuario = p_id_usuario;

    -- CONSULTA 2: Días que tienen gastos en ese mes
    SELECT DISTINCT DAY(fecha_gasto) AS dia
    FROM gastos
    WHERE id_usuario = p_id_usuario 
      AND MONTH(fecha_gasto) = p_mes 
      AND YEAR(fecha_gasto) = p_anio;

    -- CONSULTA 3: Resumen por categorías
    SELECT c.nombre_categoria, c.color_categoria, SUM(g.importe) AS total
    FROM gastos g
    JOIN categoria c ON g.id_categoria = c.id_categoria
    WHERE g.id_usuario = p_id_usuario 
      AND MONTH(g.fecha_gasto) = p_mes 
      AND YEAR(g.fecha_gasto) = p_anio
    GROUP BY g.id_categoria;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_gastos_dia` (IN `p_id_usuario` VARCHAR(36), IN `p_anio` INT, IN `p_mes` INT, IN `p_dia` INT)   BEGIN
    SELECT 
        g.id_gasto,
        g.titulo,
        g.importe,
        g.descripcion,
        g.fecha_gasto,              
        c.icono_categoria,          
        c.color_categoria,         
        g.foto_ticket
    FROM gastos g
    INNER JOIN categoria c ON g.id_categoria = c.id_categoria
    WHERE g.id_usuario = p_id_usuario
      AND YEAR(g.fecha_gasto) = p_anio
      AND MONTH(g.fecha_gasto) = p_mes
      AND DAY(g.fecha_gasto) = p_dia
    ORDER BY g.fecha_gasto DESC;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_gastos_grafico` (IN `p_id_usuario` VARCHAR(36))   BEGIN
    SELECT 
        c.nombre_categoria AS nombre,
        SUM(g.importe) AS totalGasto,
        c.color_categoria AS color
    FROM gastos g
    INNER JOIN categoria c ON g.id_categoria = c.id_categoria
    WHERE g.id_usuario = p_id_usuario 
      -- Filtramos por el mes y año actual
      AND MONTH(g.fecha_gasto) = MONTH(CURRENT_DATE())
      AND YEAR(g.fecha_gasto) = YEAR(CURRENT_DATE())
    GROUP BY c.id_categoria, c.nombre_categoria, c.color_categoria;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_movimientos` (IN `p_id_usuario` VARCHAR(36))   BEGIN
    SELECT 
        g.id_gasto, 
        g.titulo, 
        g.importe, 
        DATE_FORMAT(g.fecha_gasto, '%d/%m/%Y') AS fecha_gasto, 
        c.nombre_categoria,
        c.icono_categoria, 
        c.color_categoria
    FROM gastos g
    JOIN categoria c ON g.id_categoria = c.id_categoria
    WHERE g.id_usuario = p_id_usuario
    ORDER BY g.fecha_gasto DESC -- Solo ordenamos por la fecha del gasto
    LIMIT 5;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_pregunta` (IN `p_correo` VARCHAR(255))   BEGIN
    -- Unimos usuarios con preguntas_seguridad para dar el TEXTO al usuario
    SELECT 
        ps.pregunta AS pregunta 
    FROM usuarios u
    JOIN preguntas_seguridad ps ON u.id_pregunta_seguridad = ps.id
    WHERE u.correo = LOWER(TRIM(p_correo));
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_recuperar_pregunta` (IN `p_correo` VARCHAR(255))   BEGIN
    -- Obtenemos el hash de la respuesta de seguridad
    SELECT respuesta_seguridad 
    FROM usuarios 
    WHERE correo = LOWER(TRIM(p_correo));
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registro` (IN `p_nombre` VARCHAR(50), IN `p_correo` VARCHAR(100), IN `p_pwd` VARCHAR(255), IN `p_id_pregunta` INT, IN `p_respuesta` VARCHAR(255))   BEGIN
    -- Verificamos si el correo ya existe
    IF EXISTS (SELECT 1 FROM usuarios WHERE correo = p_correo) THEN
        SELECT 'error' AS status, 'Este correo ya está registrado.' AS message;
    ELSE
        -- Insertamos con los nombres de columnas correctos de tu tabla
        INSERT INTO usuarios (
            id_usuario, 
            nombre_usuario, 
            correo, 
            pwd,
            id_pregunta_seguridad,
            respuesta_seguridad    
        ) VALUES (
            UUID(), 
            p_nombre, 
            p_correo, 
            p_pwd,
            p_id_pregunta,
            p_respuesta
        );

        -- Confirmamos éxito
        SELECT 'success' AS status, 'Usuario registrado correctamente.' AS message;
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria`
--

CREATE TABLE `categoria` (
  `id_categoria` varchar(36) NOT NULL,
  `nombre_categoria` varchar(50) NOT NULL,
  `icono_categoria` varchar(50) NOT NULL,
  `color_categoria` varchar(7) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categoria`
--

INSERT INTO `categoria` (`id_categoria`, `nombre_categoria`, `icono_categoria`, `color_categoria`) VALUES
('4fd04ab2-0d77-11f1-aabd-88aedd238f3e', 'Vivienda', '🏠', '#2196F3'),
('4fd05c55-0d77-11f1-aabd-88aedd238f3e', 'Cuidado Personal', '✂️', '#00BCD4'),
('4fd05ccd-0d77-11f1-aabd-88aedd238f3e', 'Ocio', '🎰', '#FF5722'),
('4fd05ce7-0d77-11f1-aabd-88aedd238f3e', 'Otros gastos', '💰', '#9E9E9E'),
('4fd05d00-0d77-11f1-aabd-88aedd238f3e', 'Bares y Restaurantes', '🍔', '#FF9800'),
('4fd05d2c-0d77-11f1-aabd-88aedd238f3e', 'Ropa', '👕', '#9C27B0'),
('4fd05d3f-0d77-11f1-aabd-88aedd238f3e', 'Salud y Farmacia', '💊', '#E91E63'),
('4fd05d50-0d77-11f1-aabd-88aedd238f3e', 'Compras', '🛒', '#4CAF50'),
('4fd05d60-0d77-11f1-aabd-88aedd238f3e', 'Transporte', '🚗', '#FFC107'),
('4fd05d71-0d77-11f1-aabd-88aedd238f3e', 'Suscripciones', '📺', '#607D8B');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `gastos`
--

CREATE TABLE `gastos` (
  `id_gasto` varchar(36) NOT NULL,
  `id_usuario` varchar(36) NOT NULL,
  `id_categoria` varchar(36) NOT NULL,
  `titulo` varchar(100) NOT NULL,
  `importe` decimal(10,2) NOT NULL,
  `fecha_gasto` datetime NOT NULL,
  `descripcion` text DEFAULT NULL,
  `foto_ticket` varchar(255) DEFAULT NULL,
  `fecha_registro_gasto` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `gastos`
--

INSERT INTO `gastos` (`id_gasto`, `id_usuario`, `id_categoria`, `titulo`, `importe`, `fecha_gasto`, `descripcion`, `foto_ticket`, `fecha_registro_gasto`) VALUES
('121b0f87-161a-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd04ab2-0d77-11f1-aabd-88aedd238f3e', 'Alquiler pasado', 12.00, '2026-03-02 09:27:16', '', NULL, '2026-03-02 09:27:45'),
('4b7715a7-1618-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd04ab2-0d77-11f1-aabd-88aedd238f3e', 'Alquiler', 700.00, '2026-02-25 09:14:33', '', NULL, '2026-03-02 09:15:02'),
('6601abdc-1619-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd05ccd-0d77-11f1-aabd-88aedd238f3e', 'Casino', 10.00, '2026-03-02 09:22:27', '', NULL, '2026-03-02 09:22:56'),
('828f75b6-1620-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd05d50-0d77-11f1-aabd-88aedd238f3e', 'Chuche', 5.00, '2026-03-02 10:13:22', 'Muy duras', '', '2026-03-02 10:13:51'),
('91c90675-1619-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd05c55-0d77-11f1-aabd-88aedd238f3e', 'Pelo', 10.00, '2026-03-02 09:23:41', '', NULL, '2026-03-02 09:24:10'),
('9dcc475e-1619-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd05ce7-0d77-11f1-aabd-88aedd238f3e', 'Peaje', 23.00, '2026-03-02 09:24:01', '', NULL, '2026-03-02 09:24:30'),
('d0a1f3cb-161f-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', '4fd05ccd-0d77-11f1-aabd-88aedd238f3e', 'Tragaperras', 1.00, '2026-03-02 10:08:23', '', NULL, '2026-03-02 10:08:52');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `preguntas_seguridad`
--

CREATE TABLE `preguntas_seguridad` (
  `id` int(11) NOT NULL,
  `pregunta` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `preguntas_seguridad`
--

INSERT INTO `preguntas_seguridad` (`id`, `pregunta`) VALUES
(1, '¿Cómo se llamaba tu primera mascota?'),
(2, '¿Cuál es el nombre de tu ciudad natal?'),
(3, '¿Cómo se llama tu profesor favorito?'),
(4, '¿Cuál era la marca de tu primer coche?'),
(5, '¿En qué calle vivías a los 10 años?');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `presupuesto_mensual`
--

CREATE TABLE `presupuesto_mensual` (
  `id_presupuesto` varchar(36) NOT NULL,
  `id_usuario` varchar(36) NOT NULL,
  `limite` decimal(10,2) NOT NULL,
  `mes` tinyint(4) NOT NULL,
  `anio` smallint(6) NOT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `presupuesto_mensual`
--

INSERT INTO `presupuesto_mensual` (`id_presupuesto`, `id_usuario`, `limite`, `mes`, `anio`, `fecha_creacion`) VALUES
('42cf20d3-1618-11f1-8ef4-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', 1200.00, 3, 2026, '2026-03-02 09:14:48'),
('612852a7-10b3-11f1-8e4f-88aedd238f3e', '3d53259e-10b3-11f1-8e4f-88aedd238f3e', 1000.00, 2, 2026, '2026-02-23 12:30:04');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id_usuario` varchar(36) NOT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `pwd` varchar(255) NOT NULL,
  `id_pregunta_seguridad` int(11) NOT NULL,
  `respuesta_seguridad` varchar(255) NOT NULL,
  `fecha_registro_usuario` timestamp NOT NULL DEFAULT current_timestamp(),
  `intentos_fallidos` int(11) DEFAULT 0,
  `fecha_bloqueo` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id_usuario`, `nombre_usuario`, `correo`, `pwd`, `id_pregunta_seguridad`, `respuesta_seguridad`, `fecha_registro_usuario`, `intentos_fallidos`, `fecha_bloqueo`) VALUES
('3d53259e-10b3-11f1-8e4f-88aedd238f3e', 'Alexis', 'alexis@gmail.com', '$2y$10$mZdz6lcs3l4Q0S0.G2Od4O5Ab2sEuvi/.tLSnX.PZZpR0lUfzDqyC', 3, '$2y$10$Z/8Vb3IrvdmoohKCVFhu9uyFwVjO4xFA.HTfBTvcOK0cdIcdmqkH2', '2026-02-23 12:29:03', 0, NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`id_categoria`);

--
-- Indices de la tabla `gastos`
--
ALTER TABLE `gastos`
  ADD PRIMARY KEY (`id_gasto`),
  ADD KEY `idx_usuario_fecha` (`id_usuario`,`fecha_gasto`),
  ADD KEY `idx_categoria` (`id_categoria`);

--
-- Indices de la tabla `preguntas_seguridad`
--
ALTER TABLE `preguntas_seguridad`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `presupuesto_mensual`
--
ALTER TABLE `presupuesto_mensual`
  ADD PRIMARY KEY (`id_presupuesto`),
  ADD UNIQUE KEY `UQ_USUARIO_MES_ANIO` (`id_usuario`,`mes`,`anio`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `correo` (`correo`),
  ADD KEY `idx_registro_usuario` (`fecha_registro_usuario`);

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `gastos`
--
ALTER TABLE `gastos`
  ADD CONSTRAINT `FK_GASTO_CATEGORIA` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  ADD CONSTRAINT `FK_GASTO_USUARIO` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `presupuesto_mensual`
--
ALTER TABLE `presupuesto_mensual`
  ADD CONSTRAINT `FK_PRESUPUESTO_USUARIO` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
