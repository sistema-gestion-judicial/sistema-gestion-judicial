# Base de datos

Cada sistema tiene **su propia base de datos** dentro del mismo MySQL 8.4, y su propio usuario con
permisos solo sobre ella. Ningún sistema lee las tablas de otro: si necesita un dato de otro sistema,
lo pide por su API (los contratos están en `contracts/`).

| Módulo | Base | Usuario | Variables de entorno |
| --- | --- | --- | --- |
| oficialia | `sgj_oficialia` | `oficialia` | `OFICIALIA_DB_URL`, `OFICIALIA_DB_USER`, `OFICIALIA_DB_PASSWORD` |
| peritos | `sgj_peritos` | `peritos` | `PERITOS_DB_URL`, `PERITOS_DB_USER`, `PERITOS_DB_PASSWORD` |
| audiencias | `sgj_audiencias` | `audiencias` | `AUDIENCIAS_DB_URL`, `AUDIENCIAS_DB_USER`, `AUDIENCIAS_DB_PASSWORD` |
| tocas | `sgj_tocas` | `tocas` | `TOCAS_DB_URL`, `TOCAS_DB_USER`, `TOCAS_DB_PASSWORD` |

## Levantar MySQL en tu máquina

Necesitas Docker Desktop.

    cp .env.example .env          # cambia las contraseñas; .env nunca se sube
    docker compose up -d
    docker compose ps             # mysql debe decir "healthy"

La primera vez se crean las cuatro bases y los cuatro usuarios. Si cambias contraseñas en `.env`
después de eso, borra el volumen para que se vuelvan a crear: `docker compose down -v`.

## Conectar tu módulo

En IntelliJ, en la configuración de Tomcat, pestaña **Startup/Connection**, agrega las variables de
entorno de tu módulo (ejemplo de oficialia):

    OFICIALIA_DB_URL=jdbc:mysql://localhost:3306/sgj_oficialia
    OFICIALIA_DB_USER=oficialia
    OFICIALIA_DB_PASSWORD=<la de tu .env>

Sin esas variables el módulo de oficialia sigue funcionando con datos en memoria.

En código se usa lo que está en `common`:

    DatabaseConfig.fromEnvironment("OFICIALIA")   // lee las variables
    new Database(config).getConnection()          // conexión JDBC
    new SqlMigrator(database, classLoader)        // aplica tus scripts al arrancar

## Scripts de esquema

Van en `<modulo>/src/main/resources/db/migration/` con número y descripción:
`V001__create_catalogs.sql`, `V002__create_demand_requests.sql`. Cada script corre una sola vez y
queda registrado en la tabla `schema_history`. **Nunca edites un script que ya se subió**: para
cambiar una tabla, crea el siguiente (`V003__add_column_x.sql`).

## En la nube

En Azure cada sistema usa la misma idea: un servidor Azure Database for MySQL 8.4 con una base y un
usuario por módulo, y las mismas variables de entorno configuradas en su App Service. La guía de
despliegue está en `despliegue-azure.md`.
