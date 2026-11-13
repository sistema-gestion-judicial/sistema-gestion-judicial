# Tocas (segunda instancia)

Sistema de Seguimiento y Control de Tocas (SSCT) del monorepo `sistema-gestion-judicial`.
Java 21 · Tomcat 10.1 (`jakarta.servlet.*`) · JSP + JSTL (`jakarta.tags.core`) · Bootstrap 5.3.3
(`/webjars/bootstrap/5.3.3/`, llega por el módulo `common`).

En el código "toca" = `Appeal`, "ponencia" = `Hearing`, "bitácora" = `AuditEntry` (código en inglés,
textos de pantalla en español).

## Cómo correrlo

Desde la raíz del repo:

    ./mvnw -pl tocas -am package      # macOS o Linux
    .\mvnw.cmd -pl tocas -am package  # Windows

Genera `tocas/target/tocas.war`. Se despliega en Tomcat 10.1 (por ejemplo desde IntelliJ) y se abre en
`http://localhost:8080/tocas/`. `./mvnw verify` compila y prueba todos los módulos, como hace el CI.

## Credenciales de prueba

| Correo | Contraseña | Rol | Datos |
| --- | --- | --- | --- |
| rsalgado@utez.edu.mx | Magistrado2026! | Magistrado (Ricardo Salgado, Segunda Sala Civil) | 36 tocas, 7 ponencias, 6 avisos |
| mperez@utez.edu.mx | Perez2026! | Magistrada (María Pérez, Primera Sala Civil) | 6 tocas, 2 ponencias |

Las contraseñas se guardan con PBKDF2, no en texto plano. Son cuentas de ejemplo en memoria, solo para desarrollo.

## Datos de ejemplo

- "Hoy" está fijado en **24/10/2026** (`demo.today` en `src/main/resources/app.properties`) para que
  coincida con el diseño de Figma. Déjalo vacío para usar la fecha real.
- Toca con flujo completo para probar: **T-000418/2026** (borrador v3, finalizar, subir PDF firmado, concluir).
- Los documentos del expediente son simulados: todavía no hay archivos reales.
- Los PDF firmados que se suben se guardan en `upload.dir` (por defecto la carpeta temporal del sistema,
  subcarpeta `tocas-uploads`).

## Base de datos (pendiente)

Todavía no se conecta: la capa `dao/` usa datos en memoria (`MemoryStore`, se reinician al reiniciar Tomcat).
Cuando se conecte, se usa lo que ya ofrece `common` (ver `docs/base-de-datos.md`):

- Base `sgj_tocas`, usuario `tocas`; variables `TOCAS_DB_URL`, `TOCAS_DB_USER` y `TOCAS_DB_PASSWORD`.
- `DatabaseConfig.fromEnvironment("TOCAS")` y `Database` para la conexión; `SqlMigrator` para los scripts.
- Los scripts van en `tocas/src/main/resources/db/migration/` (`V001__...sql`) y nunca se editan una vez subidos.
- Hay que reescribir con JDBC los métodos de cada DAO y borrar `MemoryStore`.

## Pantallas y funciones

Login (error, mostrar contraseña, olvidé contraseña) · Inicio · Mis tocas (búsqueda, filtros, rango de fechas,
paginación, estado vacío) · Detalle de toca: Resumen, Expediente completo (visor con página y zoom), Ponencias
(con modal para agendar), Resolución (editor con autoguardado, vista previa e impresión, subir PDF firmado,
concluir toca) y Bitácora · Agenda (calendario y día seleccionado) · Notificaciones (todas, sin leer, leídas).

## API

| Ruta | Qué hace |
| --- | --- |
| `GET /api/v1/health` | Comprueba que la aplicación está desplegada. No pide sesión |
| `POST /api/v1/appeals/{id}/resolution/autosave` | Autoguarda el borrador de la resolución. Pide sesión |
