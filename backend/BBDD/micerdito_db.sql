-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 13-02-2026 a las 11:54:14
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
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_editar_nom_usu` (IN `p_id_usuario` VARCHAR(36), IN `p_nombre_usuario` VARCHAR(50))   BEGIN
	UPDATE usuarios
    SET nombre_usuario = p_nombre_usuario
    WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_usuario` (IN `p_id_usuario` VARCHAR(36))   BEGIN
	DELETE FROM usuarios WHERE id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_insertar_gasto` (IN `p_id_usuario` VARCHAR(36), IN `p_id_categoria` VARCHAR(36), IN `p_titulo` VARCHAR(100), IN `p_importe` DECIMAL(10,2), IN `p_fecha_gasto` DATETIME, IN `p_descripcion` TEXT)   BEGIN
    -- Generamos un UUID para el id_gasto si no lo envías desde Android
    SET @v_id_gasto = UUID();

    INSERT INTO gastos (
        id_gasto, 
        id_usuario, 
        id_categoria, 
        titulo, 
        importe, 
        fecha_gasto, 
        descripcion
    )
    VALUES (
        @v_id_gasto, 
        p_id_usuario, 
        p_id_categoria, 
        p_titulo, 
        p_importe, 
        p_fecha_gasto, 
        p_descripcion
    );

    -- Devolvemos el ID generado
    SELECT @v_id_gasto AS id_gasto;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login` (IN `p_correo` VARCHAR(100))   BEGIN
	SELECT id_usuario, nombre_usuario, correo, pwd, intentos_fallidos, 
           IF(fecha_bloqueo > NOW(), 1, 0) AS esta_bloqueado
    FROM usuarios
    WHERE correo = p_correo;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login_intentos` (IN `p_correo` VARCHAR(100), IN `p_correcto` BOOLEAN)   BEGIN
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtenerdatos` (IN `p_id_usuario` VARCHAR(36))   BEGIN
    DECLARE v_mes TINYINT;
    DECLARE v_anio SMALLINT;
    
    SET v_mes = MONTH(NOW());
    SET v_anio = YEAR(NOW());

    -- Esta consulta devuelve todo en una fila
    SELECT 
        u.nombre_usuario AS nombreUsuario,
        IFNULL((SELECT SUM(importe) FROM gastos 
                WHERE id_usuario = p_id_usuario 
                AND MONTH(fecha_gasto) = v_mes 
                AND YEAR(fecha_gasto) = v_anio), 0) AS total_dinerogastado,
        IFNULL((SELECT limite FROM presupuesto_mensual 
                WHERE id_usuario = p_id_usuario 
                AND mes = v_mes 
                AND anio = v_anio), 0) AS limite_mes,
        DATE_FORMAT(NOW(), '%M') AS mes_actual
    FROM usuarios u
    WHERE u.id_usuario = p_id_usuario;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_categorias` ()   BEGIN
	SELECT
    	id_categoria,
        nombre_categoria AS nombre,
        icono_categoria as icono,
        color_categoria as color
    FROM categoria;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registro` (IN `p_nombre_usuario` VARCHAR(50), IN `p_correo` VARCHAR(100), IN `p_pwd` VARCHAR(255), IN `p_id_respuesta` INT(11), IN `p_respuesta` VARCHAR(255))   BEGIN
    -- 1. Verificamos si el correo ya existe para evitar duplicados
    IF EXISTS (SELECT 1 FROM usuarios WHERE correo = p_correo) THEN
        SELECT 'error' AS status, 'Este correo ya está registrado.' AS message;
    ELSE
        -- 2. Insertamos generando el UUID aquí mismo
        -- Usamos UUID() de MySQL que genera los 36 caracteres
        INSERT INTO usuarios (
            id_usuario, 
            nombre_usuario, 
            correo, 
            pwd,
            id_pregunta_seguridad,
            respuesta_seguridad
        ) VALUES (
            UUID(), 
            p_nombre_usuario, 
            p_correo, 
            p_pwd,
            p_id_respuesta,
            p_respuesta
        );

        -- 3. Confirmamos éxito
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
('CAT_CASA', 'Vivienda', '🏠', '#2196F3'),
('CAT_CUIDADO_PERSONAL', 'Cuidado Personal', '✂️', '#00BCD4'),
('CAT_OCIO', 'Ocio', '🎰', '#FF5722'),
('CAT_OTROS', 'Otros gastos', '💰', '#9E9E9E'),
('CAT_RESTAURANTE', 'Bares y Restaurantes', '🍔', '#FF9800'),
('CAT_ROPA', 'Ropa', '👕', '#9C27B0'),
('CAT_SALUD', 'Salud y Farmacia', '💊', '#E91E63'),
('CAT_SUPERMERCADO', 'Compras', '🛒', '#4CAF50'),
('CAT_TRANSPORTE', 'Transporte', '🚗', '#FFC107'),
('CAT_TV', 'Suscripciones', '📺', '#607D8B');

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
  `fecha_registro_gasto` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
('c4a9a385-00f3-11f1-acc1-88aedd238f3e', 'Pepe', 'alexis@gmail.com', '$2y$10$kiTCbNqG4QFjyZqnh.D2WeIBgx02Vc9sIauOihykqKnI2JSKHD5Zq', 0, '', '2026-02-03 11:30:40', 0, NULL);

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
  ADD KEY `FK_GASTO_USUARIO` (`id_usuario`),
  ADD KEY `FK_GASTO_CATEGORIA` (`id_categoria`);

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
  ADD UNIQUE KEY `correo` (`correo`);

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
