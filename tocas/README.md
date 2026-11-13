# SSCT – Sistema de Seguimiento y Control de Tocas (Segunda Instancia)

Módulo **tocas**: Java 21 · Tomcat 10.1 (`jakarta.servlet.*`) · JSP + JSTL (`jakarta.tags.core`) · Bootstrap 5.3.3 (`/webjars/bootstrap/5.3.3/`).
En el código "toca" = `Appeal`, "ponencia" = `Hearing`, "bitácora" = `AuditEntry` (código en inglés, textos de pantalla en español).

## Cómo correrlo
1. `mvn clean package` → genera `target/tocas.war` (necesita internet la primera vez para bajar dependencias).
2. Copiar el WAR a `webapps/` de Tomcat 10.1 (o desplegarlo desde IntelliJ/Eclipse con Tomcat 10.1) y abrir `http://localhost:8080/tocas/`.

## Credenciales de prueba
| Correo | Contraseña | Rol | Datos |
|---|---|---|---|
| rsalgado@utez.edu.mx | Magistrado2026! | Magistrado (Ricardo Salgado, Segunda Sala Civil) | 36 tocas, 7 ponencias, 6 avisos (igual que Figma) |
| mperez@utez.edu.mx | Perez2026! | Magistrada (María Pérez, Primera Sala Civil) | 6 tocas, 2 ponencias |

Las contraseñas se guardan con PBKDF2 (no en texto plano).

## Datos de ejemplo
- "Hoy" está fijado a **24/10/2026** (`demo.today` en `src/main/resources/app.properties`) para que coincida con Figma. Déjalo vacío para usar la fecha real.
- Toca con flujo completo para probar: **T-000418/2026** (borrador v3 → finalizar → subir PDF firmado → concluir).
- Los documentos del expediente son **simulados** (no hay archivos reales todavía).
- PDFs firmados subidos se guardan en `upload.dir` (por defecto carpeta temporal del sistema / `tocas-uploads`).

## Base de datos (pendiente)
Todavía no existe, así que `dao/` usa datos en memoria (`MemoryStore`; se reinician al reiniciar Tomcat).
Cuando exista: ejecutar `sql/schema.sql`, configurar `db.*` en `app.properties` y reescribir los métodos de cada DAO con JDBC usando `DbConnection`.

## Pantallas / funciones
Login (error, focus, mostrar contraseña, olvidé contraseña) · Inicio · Mis tocas (búsqueda, filtros, rango de fechas, paginación, estado vacío) · Detalle de toca: Resumen, Expediente completo (visor con página y zoom), Ponencias (+ modal agendar), Resolución (editor, vista previa e impresión, subir PDF firmado, concluir toca), Bitácora · Agenda (calendario, día seleccionado) · Notificaciones (Todas / Sin leer / Leídas, marcar leídas).

git switch main
git pull
git switch -c repo/b    uild/1-init-maven-db
git add pom.xml .gitignore sql/schema.sql
git commit -m "build: add base maven config and sql schemas"
git push -u origin repo/build/1-init-maven-db