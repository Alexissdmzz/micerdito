-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 27-01-2026 a las 11:58:40
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
CREATE DATABASE IF NOT EXISTS `micerdito_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `micerdito_db`;

DELIMITER $$
--
-- Procedimientos
--
DROP PROCEDURE IF EXISTS `sp_login`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login` (IN `P_CORREO` VARCHAR(100))   BEGIN 
SELECT ID_USUARIO, NOMBRE_USUARIO, CORREO, CONTRASEÑA, INTENTOS_FALLIDOS, 
           IF(FECHA_BLOQUEO > NOW(), 1, 0) AS esta_bloqueado
    FROM usuarios
    WHERE CORREO = P_CORREO;
END$$

DROP PROCEDURE IF EXISTS `sp_login_intentos`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_login_intentos` (IN `P_CORREO` VARCHAR(100), IN `P_CORRECTO` BOOLEAN)   BEGIN
    IF P_CORRECTO THEN
        -- Si entró bien, reseteamos intentos y quitamos bloqueo
        UPDATE usuarios 
        SET INTENTOS_FALLIDOS = 0, FECHA_BLOQUEO = NULL 
        WHERE CORREO = P_CORREO;
    ELSE
        -- Si falló, sumamos uno al contador actual
        UPDATE usuarios 
        SET INTENTOS_FALLIDOS = INTENTOS_FALLIDOS + 1 
        WHERE CORREO = P_CORREO;

        -- Si llega a 3 o más, bloqueamos por 15 minutos desde este momento
        UPDATE usuarios 
        SET FECHA_BLOQUEO = DATE_ADD(NOW(), INTERVAL 15 MINUTE) 
        WHERE CORREO = P_CORREO AND INTENTOS_FALLIDOS >= 3;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `sp_registro`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registro` (IN `P_NOMBRE_USUARIO` VARCHAR(50), IN `P_CORREO` VARCHAR(100), IN `P_CONTRASEÑA` VARCHAR(255))   BEGIN
    -- 1. Verificamos si el correo ya existe para evitar duplicados
    IF EXISTS (SELECT 1 FROM usuarios WHERE CORREO = p_correo) THEN
        SELECT 'error' AS status, 'Este correo ya está registrado.' AS message;
    ELSE
        -- 2. Insertamos generando el UUID aquí mismo
        -- Usamos UUID() de MySQL que genera los 36 caracteres
        INSERT INTO usuarios (
            ID_USUARIO, 
            NOMBRE_USUARIO, 
            CORREO, 
            CONTRASEÑA
        ) VALUES (
            UUID(), 
            p_nombre_usuario, 
            p_correo, 
            p_contraseña
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

DROP TABLE IF EXISTS `categoria`;
CREATE TABLE IF NOT EXISTS `categoria` (
  `ID_CATEGORIA` varchar(36) NOT NULL,
  `NOMBRE_CATEGORIA` varchar(50) NOT NULL,
  `ICONO_CATEGORIA` varchar(50) NOT NULL,
  `COLOR_CATEGORIA` varchar(7) NOT NULL,
  PRIMARY KEY (`ID_CATEGORIA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `gastos`
--

DROP TABLE IF EXISTS `gastos`;
CREATE TABLE IF NOT EXISTS `gastos` (
  `ID_GASTO` varchar(36) NOT NULL,
  `ID_USUARIO` varchar(36) NOT NULL,
  `ID_CATEGORIA` varchar(36) NOT NULL,
  `TITULO` varchar(100) NOT NULL,
  `IMPORTE` decimal(10,2) NOT NULL,
  `FECHA_GASTO` datetime NOT NULL,
  `DESCRIPCION` text DEFAULT NULL,
  `FECHA_REGISTRO_GASTO` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID_GASTO`),
  KEY `FK_GASTO_USUARIO` (`ID_USUARIO`),
  KEY `FK_GASTO_CATEGORIA` (`ID_CATEGORIA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `presupuesto_mensual`
--

DROP TABLE IF EXISTS `presupuesto_mensual`;
CREATE TABLE IF NOT EXISTS `presupuesto_mensual` (
  `ID_PRESUPUESTO` varchar(36) NOT NULL,
  `ID_USUARIO` varchar(36) NOT NULL,
  `LIMITE` decimal(10,2) NOT NULL,
  `MES` tinyint(4) NOT NULL,
  `ANIO` smallint(6) NOT NULL,
  `FECHA_CREACION` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`ID_PRESUPUESTO`),
  UNIQUE KEY `UQ_USUARIO_MES_ANIO` (`ID_USUARIO`,`MES`,`ANIO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE IF NOT EXISTS `usuarios` (
  `ID_USUARIO` varchar(36) NOT NULL,
  `NOMBRE_USUARIO` varchar(50) NOT NULL,
  `CORREO` varchar(100) NOT NULL,
  `CONTRASEÑA` varchar(255) NOT NULL,
  `FECHA_REGISTRO_USUARIO` timestamp NOT NULL DEFAULT current_timestamp(),
  `INTENTOS_FALLIDOS` int(11) DEFAULT 0,
  `FECHA_BLOQUEO` datetime DEFAULT NULL,
  PRIMARY KEY (`ID_USUARIO`),
  UNIQUE KEY `CORREO` (`CORREO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`ID_USUARIO`, `NOMBRE_USUARIO`, `CORREO`, `CONTRASEÑA`, `FECHA_REGISTRO_USUARIO`, `INTENTOS_FALLIDOS`, `FECHA_BLOQUEO`) VALUES
('7f893c4b-fb6e-11f0-9448-88aedd238f3e', 'Alexis', 'Alexiss@gmail.com', '$2y$10$n/RSGtuD2Uze6N9YvlRUn.MaFlscCXBS2z3I0NsP0168fdWLKLzCW', '2026-01-27 10:54:05', 0, NULL);

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `gastos`
--
ALTER TABLE `gastos`
  ADD CONSTRAINT `FK_GASTO_CATEGORIA` FOREIGN KEY (`ID_CATEGORIA`) REFERENCES `categoria` (`ID_CATEGORIA`),
  ADD CONSTRAINT `FK_GASTO_USUARIO` FOREIGN KEY (`ID_USUARIO`) REFERENCES `usuarios` (`ID_USUARIO`) ON DELETE CASCADE;

--
-- Filtros para la tabla `presupuesto_mensual`
--
ALTER TABLE `presupuesto_mensual`
  ADD CONSTRAINT `FK_PRESUPUESTO_USUARIO` FOREIGN KEY (`ID_USUARIO`) REFERENCES `usuarios` (`ID_USUARIO`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
