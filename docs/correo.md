# Correo (recuperar contraseña)

Oficialía envía por correo el código de verificación de **Recuperar contraseña** (RF 1.3). El código
vence a los 15 minutos y solo se usa una vez. Solo las cuentas activas lo reciben, y la pantalla nunca
revela si un correo está registrado.

## Configuración

Cada valor se toma, en este orden, de la variable de entorno, de `mail.properties` y de un valor por
defecto.

| Variable | Para qué | Por defecto |
| --- | --- | --- |
| `OFICIALIA_MAIL_HOST` | Servidor SMTP. **Sin él no se envía nada.** | vacío |
| `OFICIALIA_MAIL_PORT` | Puerto | `587` |
| `OFICIALIA_MAIL_USER` / `OFICIALIA_MAIL_PASSWORD` | Cuenta que envía | vacío |
| `OFICIALIA_MAIL_FROM` / `OFICIALIA_MAIL_FROM_NAME` | Remitente | el usuario / `SCD-2026 Oficialía de Partes` |
| `OFICIALIA_MAIL_AUTH` / `OFICIALIA_MAIL_STARTTLS` | Autenticación y STARTTLS | `true` / `true` |
| `OFICIALIA_MAIL_LOG_ONLY` | **Solo desarrollo:** escribe el correo en el log en lugar de enviarlo | `false` |

Con Gmail usa una cuenta personal con verificación en dos pasos y una **contraseña de aplicación** de
16 caracteres. Las cuentas institucionales suelen bloquear SMTP (error 535).

## En tu máquina

Opción 1, sin servidor de correo: en la configuración de Tomcat de IntelliJ agrega
`OFICIALIA_MAIL_LOG_ONLY=true` y busca el código en el log de Tomcat (línea `Mail log-only mode`).

Opción 2, correo real: copia `oficialia/src/main/resources/mail.properties.example` como
`mail.properties` en la misma carpeta y rellénalo. Ese archivo está en `.gitignore`: nunca lo subas.

Con Docker Compose define las variables en `.env` (hay ejemplos comentados en `.env.example`).

## En Heroku

    heroku config:set --app $APP \
      OFICIALIA_MAIL_HOST="smtp.gmail.com" \
      OFICIALIA_MAIL_USER="cuenta@gmail.com" \
      OFICIALIA_MAIL_PASSWORD="contraseña-de-aplicación"

No actives `OFICIALIA_MAIL_LOG_ONLY` en producción: el log mostraría los códigos.

## Límites contra abuso

Por cada correo (esté registrado o no, para no revelar qué cuentas existen) se aceptan como máximo
**1 código cada 60 segundos y 5 por hora** (`AuthSettings.RECOVERY_REQUEST_COOLDOWN_SECONDS` y
`RECOVERY_MAX_REQUESTS_PER_HOUR`). Una solicitud que se pasa del límite recibe la misma respuesta que
las demás, pero no genera código ni correo, y el código vigente conserva sus intentos fallidos. Así
nadie puede seguir adivinando un código pidiendo otro, ni llenar un buzón ni agotar el cupo diario del
servidor SMTP (Gmail permite unos 500 al día).

Los correos salen por hilos propios (`MailExecutor`: 2 hilos y una cola de 50), no por el pool común de
la JVM. Si la cola se llena, el mensaje se descarta y queda un aviso en el log. Los hilos se detienen al
parar la aplicación (`MailShutdownListener`).

## Cómo está armado

`PasswordRecoveryService` decide las reglas, `EmailService` elige asunto y texto
(`EmailContentBuilder`) y `MailClient` solo transporta: `SmtpMailClient` (Jakarta Mail) o
`SampleMailClient` (solo log). Las cuentas vienen de `OficialiaDaos.users()` (MySQL o memoria, según
`OFICIALIA_DB_URL`). Los códigos siguen en memoria detrás de `RecoveryCodeDao`; la tabla
`password_reset_codes` (V005) permitiría guardarlos en la base para que sobrevivan a un reinicio.
