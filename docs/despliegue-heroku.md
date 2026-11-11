# Despliegue de Oficialía en Heroku

Cada módulo se despliega por su cuenta, donde prefiera su equipo. Oficialía se publica completa (las
pantallas y la API) en Heroku, con una base MySQL de JawsDB.

| Pieza | Costo |
| --- | --- |
| App de Heroku con dyno **Basic** (siempre encendido) | 7 USD/mes |
| Base JawsDB MySQL, plan **Kitefin** | Gratis (5 MB, 10 conexiones, 3,600 consultas por hora) |

Para que las 3,600 consultas alcancen, Oficialía no revisa en la base en cada clic que la cuenta siga activa:
confía un minuto en la última revisión (`AuthSettings.ACTIVE_ACCOUNT_CHECK_SECONDS`). Si desactivas a
alguien directo en la base, su sesión se cierra a más tardar un minuto después.

Todos los comandos van en **Git Bash**, desde la raíz del repo.

## 1. Preparar

1. Instala la [Heroku CLI](https://cli-assets.heroku.com/channels/stable/heroku-x64.exe) y entra a tu cuenta:
   ```bash
   heroku login
   ```
2. Descarga el `.jar` de la [última versión del Heroku JVM Application Deployer](https://github.com/heroku/heroku-jvm-application-deployer/releases/latest)
   y guárdalo fuera del repo como `~/tools/heroku-jvm-application-deployer.jar`.

## 2. Crear la app y la base

```bash
APP=sgj-oficialia        # must be unique in Heroku; add a suffix if it is taken
heroku apps:create $APP --region us --no-remote
heroku addons:create jawsdb:kitefin --app $APP --wait
heroku config:get JAWSDB_URL --app $APP
```

El último comando imprime algo así:

```
mysql://USUARIO:CONTRASEÑA@HOST:3306/NOMBRE_BASE
```

## 3. Variables

Copia esos datos en las variables de Oficialía. La URL de JDBC no lleva usuario ni contraseña:

```bash
heroku config:set --app $APP \
  OFICIALIA_DB_URL="jdbc:mysql://HOST:3306/NOMBRE_BASE" \
  OFICIALIA_DB_USER="USUARIO" \
  OFICIALIA_DB_PASSWORD="CONTRASEÑA" \
  OFICIALIA_API_KEYS="audiencias=CLAVE1;tocas=CLAVE2;peritos=CLAVE3" \
  OFICIALIA_ADMIN_EMAIL="correo@del-administrador" \
  OFICIALIA_ADMIN_PASSWORD="CONTRASEÑA-SEGURA" \
  TZ=America/Mexico_City
```

- `OFICIALIA_ADMIN_EMAIL` y `OFICIALIA_ADMIN_PASSWORD` crean al Administrador del órgano judicial en el
  primer arranque con la tabla de usuarios vacía (contraseña de 8 caracteres o más, con letras y
  números). Después ya no cambian nada: puedes borrarlas. **Nunca** pongas `OFICIALIA_SAMPLE_USERS` en
  producción.

- Las claves son para los otros equipos. Genera cada una con `openssl rand -hex 24` y pásasela a
  cada equipo por privado.
- `TZ` hace que las fechas se guarden con la hora de la Ciudad de México.
- Las tablas y los datos de ejemplo se crean solos al arrancar.

## 4. Desplegar

```bash
./mvnw -pl oficialia -am clean verify
java -jar ~/tools/heroku-jvm-application-deployer.jar --app=$APP --jdk=21 \
  --webapp-runner-version=10.1.61.0 oficialia/target/oficialia.war
heroku ps:type web=basic --app $APP
```

Heroku corre el `.war` en Tomcat 10.1. La app queda en la raíz del dominio, sin `/oficialia`.

## 5. Comprobar

```bash
heroku apps:info --app $APP      # the "Web URL" line has the address
heroku logs --tail --app $APP    # live log, if something fails
```

Abre la Web URL en el navegador y entra con tu usuario de siempre. La API queda en
`<Web URL>/api/v1/...`: pon esa URL en `servers` de `contracts/oficialia.yaml` y avisa a los otros
equipos.

Para actualizar después de un cambio, repite el paso 4.

## Si algo falla

| Error en el log | Arreglo |
| --- | --- |
| `Access denied for user` | Usuario o contraseña distintos a los de `JAWSDB_URL`: repite el paso 3 |
| `Communications link failure` | Revisa que la URL sea `jdbc:mysql://HOST:3306/NOMBRE_BASE` |
| `exceeded the 'max_questions' resource` | Se acabaron las consultas por hora de Kitefin: espera una hora o sube de plan en el panel de JawsDB |

## Apagar

```bash
heroku ps:scale web=0 --app $APP                  # stops the app, keeps the data
heroku apps:destroy --app $APP --confirm $APP     # deletes everything
```

Fuentes: [Heroku JVM Application Deployer](https://devcenter.heroku.com/articles/war-deployment),
[webapp-runner](https://github.com/heroku/webapp-runner), [JawsDB MySQL](https://devcenter.heroku.com/articles/jawsdb),
[precios de Heroku](https://www.heroku.com/pricing).
