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

### Usuarios de Oficialía

Con base de datos, las cuentas viven en la tabla `users` y no hay cuentas fijas en el código. Al
arrancar con esa tabla vacía se crean:

| Variable | Qué crea |
| --- | --- |
| `OFICIALIA_ADMIN_EMAIL` y `OFICIALIA_ADMIN_PASSWORD` | El Administrador del órgano judicial (contraseña de 8 caracteres o más, con letras y números). Opcionales: `OFICIALIA_ADMIN_FIRST_NAME` y `OFICIALIA_ADMIN_LAST_NAME` |
| `OFICIALIA_SAMPLE_USERS=true` | Las cuentas demo (oficial de partes, administradores y una cuenta inactiva). **Solo para desarrollo** |

En local pon las dos cosas en tu `.env` (ya vienen en `.env.example`) o en las variables de Tomcat de
IntelliJ. Si tu base ya tenía usuarios, no pasa nada: estas variables solo actúan con la tabla vacía.
Sin ninguna de ellas, el log avisa que nadie puede iniciar sesión.

Después, el Administrador de Juzgado registra a los oficiales de partes de su juzgado desde **Usuarios**
(`/users`). El sistema genera una contraseña temporal de 12 caracteres y la muestra una sola vez al guardar;
en la base solo queda su hash. La primera vez que el usuario entra, el sistema lo manda a cambiarla
(`must_change_password`) y no le deja abrir otra pantalla hasta que lo haga. Sin base de datos, los usuarios
registrados viven en memoria y se pierden al reiniciar.

En código se usa lo que está en `common`:

    DatabaseConfig.fromEnvironment("OFICIALIA")   // lee las variables
    new Database(config).getConnection()          // conexión JDBC
    new SqlMigrator(database, classLoader)        // aplica tus scripts al arrancar

## Scripts de esquema

Van en `<modulo>/src/main/resources/db/migration/` con número y descripción:
`V001__create_catalogs.sql`, `V002__create_demand_requests.sql`. Cada script corre una sola vez y
queda registrado en la tabla `schema_history`. **Nunca edites un script que ya se subió**: para
cambiar una tabla, crea el siguiente (`V003__add_column_x.sql`).

## Tablas de Oficialía

| Tabla | Qué guarda |
| --- | --- |
| `roles` | Los tres perfiles (oficial de partes, administrador de juzgado, administrador del órgano) |
| `users`, `password_reset_codes` | Cuentas, bloqueo por intentos y códigos de recuperación |
| `judicial_districts`, `courts`, `subject_matters`, `court_subject_matters` | Catálogos; las materias tienen clave estable (`CIV`, `FAM`…) para la integración |
| `rejection_reasons` | Motivos de no procedencia |
| `demand_requests`, `parties`, `annexes` | Solicitudes con sus partes y anexos. Desde V007 guardan el tipo de presentación (individual o colectiva), las observaciones, el domicilio de cada parte y el tipo de cada anexo |
| `duplicity_warning_decisions` | Cada advertencia de duplicidad y si el oficial continuó o canceló (un intento cancelado no tiene folio) |
| `case_files` | Expedientes `AAAA/NNNNN/JJ`, su mesa, su instancia y su sala |
| `request_status_history` | Bitácora de cambios de estado de cada solicitud |
| `second_instance_transfers` | Bitácora de pases a segunda instancia |

`JdbcDemandRequestDao` llena `case_files`, `duplicity_warning_decisions` y `request_status_history` en la
misma transacción en que guarda la solicitud. Los datos semilla (catálogos y roles) vienen en los propios
scripts.

De cada anexo se guardan el nombre, el tamaño y el tipo, no el archivo: el plan Kitefin de JawsDB
tiene 5 MB en total y los archivos lo llenarían. Guardar los archivos queda pendiente de decidir dónde.

## En la nube

Cada equipo despliega su módulo donde prefiera, con su propia base en la nube y las mismas variables
de entorno que usa en local. Oficialía usa Heroku con JawsDB MySQL: la guía está en
`docs/despliegue-heroku.md`.
