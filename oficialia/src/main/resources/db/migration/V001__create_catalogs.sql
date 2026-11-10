-- Catalogs that the other systems also read through the API: courts and subject matters

CREATE TABLE courts (
                        code CHAR(2) PRIMARY KEY,
                        name VARCHAR(120) NOT NULL UNIQUE,
                        judicial_district VARCHAR(120) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE subject_matters (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 name VARCHAR(60) NOT NULL UNIQUE,
                                 active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO courts (code, name, judicial_district) VALUES
                                                       ('01', 'Juzgado de Cuernavaca', 'Distrito judicial de Cuernavaca'),
                                                       ('05', 'Juzgado de Yautepec', 'Distrito judicial de Yautepec'),
                                                       ('07', 'Juzgado de Jojutla', 'Distrito judicial de Jojutla'),
                                                       ('12', 'Juzgado de Cuautla', 'Distrito judicial de Cuautla');

INSERT INTO subject_matters (name) VALUES ('Civil'), ('Familiar'), ('Mercantil'), ('Penal');
