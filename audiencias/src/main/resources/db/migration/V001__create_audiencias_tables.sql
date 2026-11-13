
CREATE TABLE usuarios (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    cedula_profesional VARCHAR(15) NULL,
    rol                ENUM('ADMINISTRADOR', 'ABOGADO') NOT NULL,
    estado             TINYINT(1) NOT NULL DEFAULT 1,
    correo             VARCHAR(150) NOT NULL UNIQUE,
    password           VARCHAR(255) NOT NULL,
    nombre_completo    VARCHAR(150) NOT NULL,
    telefono           VARCHAR(15) NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_rol ON usuarios (rol);
CREATE INDEX idx_usuarios_correo ON usuarios (correo);


CREATE TABLE caso (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    numero_expediente VARCHAR(20) NOT NULL UNIQUE,
    id_juzgado        VARCHAR(2) NOT NULL,
    materia           VARCHAR(30) NOT NULL,
    demandado         VARCHAR(150) NULL,
    demandante        VARCHAR(150) NULL,
    estado            TINYINT(1) NOT NULL DEFAULT 1,
    creacion          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cierre            DATETIME NULL
);

CREATE INDEX idx_caso_expediente ON caso (numero_expediente);
CREATE INDEX idx_caso_juzgado_materia ON caso (id_juzgado, materia);


CREATE TABLE mesas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre_mesa VARCHAR(50) NOT NULL,
    materia     VARCHAR(30) NOT NULL,
    activo      TINYINT(1) NOT NULL DEFAULT 1,
    id_usuario  INT NULL,
    id_juzgado  VARCHAR(2) NULL,
    CONSTRAINT fk_mesa_abogado
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id)
            ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_mesas_usuario ON mesas (id_usuario);
CREATE INDEX idx_mesas_materia ON mesas (materia);
CREATE INDEX idx_mesas_juzgado ON mesas (id_juzgado);


CREATE TABLE expedientes (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    id_caso      INT NULL,
    url          VARCHAR(500) NOT NULL,
    descripcion  VARCHAR(200) NULL,
    fecha_subida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expediente_caso
        FOREIGN KEY (id_caso) REFERENCES caso (id)
            ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_expedientes_caso ON expedientes (id_caso);

CREATE TABLE audiencia (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    fecha         DATETIME NULL,
    estado        ENUM('PENDIENTE', 'PROCESO', 'FINALIZADA') NOT NULL DEFAULT 'PENDIENTE',
    id_mesa       INT NULL,
    id_caso       INT NULL,
    id_abogado    INT NULL,
    sala          VARCHAR(50) NULL,
    observaciones TEXT NULL,
    CONSTRAINT fk_audiencia_abogado
        FOREIGN KEY (id_abogado) REFERENCES usuarios (id)
            ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_audiencia_caso
        FOREIGN KEY (id_caso) REFERENCES caso (id)
            ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_audiencia_mesa
        FOREIGN KEY (id_mesa) REFERENCES mesas (id)
            ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX idx_audiencia_fecha ON audiencia (fecha);
CREATE INDEX idx_audiencia_estado ON audiencia (estado);
CREATE INDEX idx_audiencia_mesa ON audiencia (id_mesa);
CREATE INDEX idx_audiencia_caso ON audiencia (id_caso);
CREATE INDEX idx_audiencia_abogado ON audiencia (id_abogado);

INSERT INTO usuarios (cedula_profesional, rol, estado, correo, password, nombre_completo, telefono) VALUES
    (NULL, 'ADMINISTRADOR', 1, 'admin.audiencias@utez.edu.mx', 'admin123', 'Administrador General Audiencias', '7771234567'),
    ('12345678', 'ABOGADO', 1, 'abogado.civil@utez.edu.mx', 'abogado123', 'Lic. Carlos Mendoza Ramos', '7772345678'),
    ('23456789', 'ABOGADO', 1, 'abogado.familiar@utez.edu.mx', 'abogado123', 'Lic. Ana Gómez Peralta', '7773456789'),
    ('34567890', 'ABOGADO', 1, 'abogado.mercantil@utez.edu.mx', 'abogado123', 'Lic. Roberto Silva Fuentes', '7774567890'),
    ('45678901', 'ABOGADO', 1, 'abogado.penal@utez.edu.mx', 'abogado123', 'Lic. Diana Morales Vega', '7775678901');

INSERT INTO mesas (nombre_mesa, materia, activo, id_usuario, id_juzgado) VALUES
    ('Mesa 1', 'Civil', 1, 2, '12'),
    ('Mesa 2', 'Familiar', 1, 3, '12'),
    ('Mesa 3', 'Mercantil', 1, 4, '12'),
    ('Mesa 4', 'Penal', 1, 5, '12');

-- Caso de prueba sincronizado con el formato del número de expediente de Oficialía
INSERT INTO caso (numero_expediente, id_juzgado, materia, demandado, demandante, estado) VALUES
    ('2026/00431/12', '12', 'Penal', 'Demandado Desconocido', 'Juan Ruiz Hernández', 1);

INSERT INTO audiencia (fecha, estado, id_mesa, id_caso, id_abogado, sala, observaciones) VALUES
    ('2026-11-20 10:00:00', 'PENDIENTE', 4, 1, 5, 'Sala 1 - Oralidad Penal', 'Audiencia inicial de formulación de imputación');
