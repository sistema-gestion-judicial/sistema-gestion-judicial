#!/bin/bash
# Creates one database and one user per module. Each user can only touch its own database,
# so no system reads another system's tables: they talk through their APIs.
set -euo pipefail

mysql --user=root --password="${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS sgj_oficialia CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS sgj_peritos CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS sgj_audiencias CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS sgj_tocas CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'oficialia'@'%' IDENTIFIED BY '${OFICIALIA_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'peritos'@'%' IDENTIFIED BY '${PERITOS_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'audiencias'@'%' IDENTIFIED BY '${AUDIENCIAS_DB_PASSWORD}';
CREATE USER IF NOT EXISTS 'tocas'@'%' IDENTIFIED BY '${TOCAS_DB_PASSWORD}';

GRANT ALL PRIVILEGES ON sgj_oficialia.* TO 'oficialia'@'%';
GRANT ALL PRIVILEGES ON sgj_peritos.* TO 'peritos'@'%';
GRANT ALL PRIVILEGES ON sgj_audiencias.* TO 'audiencias'@'%';
GRANT ALL PRIVILEGES ON sgj_tocas.* TO 'tocas'@'%';
FLUSH PRIVILEGES;
SQL
