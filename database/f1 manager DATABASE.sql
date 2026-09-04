CREATE DATABASE IF NOT EXISTS f1_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE f1_manager;

CREATE TABLE equipos (
    nombre      VARCHAR(100) NOT NULL PRIMARY KEY,
    pais        VARCHAR(100) NOT NULL,
    motor       VARCHAR(100) NOT NULL,
    imagen_url  VARCHAR(500)
) ENGINE=InnoDB;

CREATE TABLE circuitos (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    pais          VARCHAR(100) NOT NULL,
    longitud_km   DOUBLE NOT NULL,
    vueltas       INT NOT NULL,
    descripcion   VARCHAR(1000)
) ENGINE=InnoDB;

CREATE TABLE pilotos (
    id                          INT AUTO_INCREMENT PRIMARY KEY,
    nombre                      VARCHAR(150) NOT NULL,
    equipo                      VARCHAR(100) NOT NULL,
    rol                         VARCHAR(20) NOT NULL,
    experiencia_anios           INT NOT NULL,
    habilidad_curva             INT NOT NULL,
    habilidad_adelantamiento    INT NOT NULL,
    habilidad_recta             INT NOT NULL,
    habilidad_lluvia            INT NOT NULL,
    habilidad_seco              INT NOT NULL,
    habilidad_extremo           INT NOT NULL,
    imagen_url                  VARCHAR(500),
    CONSTRAINT fk_pilotos_equipo FOREIGN KEY (equipo) REFERENCES equipos(nombre)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE vehiculos (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    modelo                VARCHAR(150) NOT NULL,
    equipo                VARCHAR(100) NOT NULL,
    motor                 VARCHAR(100) NOT NULL,
    velocidad_max_kmh     DOUBLE NOT NULL,
    aceleracion_0_100     DOUBLE NOT NULL,
    carga_aerodinamica    VARCHAR(20) NOT NULL,
    modo_conduccion       VARCHAR(20) NOT NULL,
    tipo_neumatico        VARCHAR(20) NOT NULL,
    presion_aire          DOUBLE NOT NULL,
    CONSTRAINT fk_vehiculos_equipo FOREIGN KEY (equipo) REFERENCES equipos(nombre)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_vehiculos_equipo UNIQUE (equipo)
) ENGINE=InnoDB;
