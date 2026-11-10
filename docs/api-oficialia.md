# Cómo consumir la API de Oficialía

Oficialía de Partes comparte sus catálogos (juzgados y materias) y sus expedientes con Audiencias,
Tocas y Peritos. Es una API de solo lectura: peticiones `GET` con respuestas en JSON.

| Dato | Valor |
| --- | --- |
| URL de producción | `https://sgj-oficialia-ad65e5f5fb37.herokuapp.com` |
| URL local (Tomcat de IntelliJ) | `http://localhost:8080/oficialia` |
| URL local (Docker Compose) | `http://localhost:8081` |
| Autenticación | Encabezado `X-Api-Key` con la clave de tu sistema |
| Formato | JSON en UTF-8, propiedades en camelCase, fechas en ISO 8601 |
| Contrato completo | [`contracts/oficialia.yaml`](../contracts/oficialia.yaml) |

## 1. Pide tu clave

Cada sistema tiene su propia clave. Pídesela al equipo de Oficialía por mensaje privado y guárdala
en una variable de entorno de tu sistema (por ejemplo `OFICIALIA_API_KEY`), **nunca en el código ni
en el repo**. Sin clave, o con una clave que no existe, la API responde `401`.

Para comprobar que la API está arriba, `/api/v1/health` no pide clave:

```bash
curl https://sgj-oficialia-ad65e5f5fb37.herokuapp.com/api/v1/health
# {"system":"oficialia","status":"UP","database":"UP"}
```

## 2. Endpoints

| Método y ruta | Devuelve |
| --- | --- |
| `GET /api/v1/health` | Estado del sistema y de su base. No pide clave |
| `GET /api/v1/courts` | Todos los juzgados, ordenados por nombre |
| `GET /api/v1/courts/{code}` | Un juzgado por su clave de dos dígitos, por ejemplo `12` |
| `GET /api/v1/subject-matters` | Todas las materias (Civil, Familiar, Mercantil, Penal) |
| `GET /api/v1/case-files` | Los expedientes, del más reciente al más antiguo, con filtros opcionales |
| `GET /api/v1/case-files/{año}/{número}/{juzgado}` | Un expediente por su número |

Filtros de `/api/v1/case-files` (se pueden combinar):

| Parámetro | Ejemplo | Efecto |
| --- | --- | --- |
| `courtCode` | `12` | Solo los de ese juzgado |
| `subjectMatter` | `Familiar` | Solo los de esa materia, sin importar mayúsculas |
| `admittedSince` | `2026-11-01` | Solo los calificados como procedentes desde esa fecha |

Un expediente es una solicitud de demanda calificada como procedente. Su número tiene la forma
`AAAA/NNNNN/JJ` (año, consecutivo y juzgado) y en la URL va con sus diagonales:
`2026/00431/12` se pide como `/api/v1/case-files/2026/00431/12`.

## 3. Ejemplo con curl

```bash
KEY="tu-clave"
URL=https://sgj-oficialia-ad65e5f5fb37.herokuapp.com

curl -H "X-Api-Key: $KEY" $URL/api/v1/courts
curl -H "X-Api-Key: $KEY" "$URL/api/v1/case-files?courtCode=12&subjectMatter=Penal"
curl -H "X-Api-Key: $KEY" $URL/api/v1/case-files/2026/00431/12
```

En PowerShell usa `curl.exe` en vez de `curl`, o mejor Git Bash.

Respuesta del último:

```json
{
  "data": {
    "caseFileNumber": "2026/00431/12",
    "requestFolio": "S-000124/2026",
    "court": { "code": "12", "name": "Juzgado de Cuautla" },
    "subjectMatter": "Penal",
    "receivedDate": "2026-10-23",
    "admittedAt": "2026-10-24T10:15:00",
    "hearingDesk": "Mesa 2",
    "hearingDeskPending": false,
    "plaintiff": { "identified": true, "name": "Juan Ruiz Hernández", "personType": "INDIVIDUAL" },
    "defendant": { "identified": false, "name": null, "personType": null },
    "legalRepresentative": null,
    "annexCount": 1
  }
}
```

- Las listas vienen en `data` como arreglo, y `/case-files` agrega `count`.
- `admittedAt` es fecha y hora local de la Ciudad de México, sin zona horaria.
- `identified: false` significa demandante anónimo o demandado desconocido; por eso `name` es `null`.
- `hearingDesk` es `null` mientras Audiencias no asigna la mesa (`hearingDeskPending: true`).
- Los teléfonos, correos y el RFC o CURP de las partes no salen de Oficialía.

## 4. Ejemplo en Postman

La forma más rápida es importar la colección que ya está en el repo:

1. En Postman, **Import** → elige el archivo `contracts/oficialia.postman_collection.json`.
2. Abre la colección **Oficialía de Partes (SCD-2026)** → pestaña **Variables**.
3. En `apiKey`, escribe tu clave en la columna **Current value** y guarda. Ese valor se queda en tu
   computadora y no se sube si compartes la colección.
4. Abre cualquier petición y presiona **Send**. La clave ya viaja sola en el encabezado `X-Api-Key`,
   porque la colección la configura en su pestaña **Authorization**.

La colección trae las seis peticiones y una prueba en cada una que revisa el código de respuesta;
su resultado aparece en la pestaña **Test Results**. `baseUrl` ya apunta a producción; cámbialo a
`http://localhost:8080/oficialia` para probar contra tu Tomcat local.

Si prefieres armar una petición a mano:

1. **New** → **HTTP**, método `GET`, URL `https://sgj-oficialia-ad65e5f5fb37.herokuapp.com/api/v1/courts`.
2. Pestaña **Authorization** → tipo **API Key** → Key: `X-Api-Key`, Value: tu clave, Add to: **Header**.
3. **Send**. Debe responder `200 OK` con los juzgados en `data`.

Postman también puede importar directamente `contracts/oficialia.yaml` y generar la colección desde
el contrato; la del repo ya trae las variables y la clave configuradas.

## 5. Ejemplo en Java

Todos los módulos heredan Jackson de `common`, y Java 21 trae `HttpClient`, así que no hace falta
agregar nada al `pom.xml`. Este cliente busca un expediente y convierte la respuesta en un `record`:

```java
package mx.edu.utez.judicial.audiencias.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import mx.edu.utez.judicial.common.api.ApiJson;

/** Reads case files from the Oficialía API. */
public class OficialiaClient {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CaseFile(String caseFileNumber, String subjectMatter, LocalDate receivedDate,
                           LocalDateTime admittedAt, String hearingDesk) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CaseFileResponse(CaseFile data) {
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String baseUrl = System.getenv("OFICIALIA_API_URL");
    private final String apiKey = System.getenv("OFICIALIA_API_KEY");

    /** Empty when the case file does not exist. */
    public Optional<CaseFile> findCaseFile(String number) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/case-files/" + number))
                .header("X-Api-Key", apiKey)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return switch (response.statusCode()) {
            case 200 -> Optional.of(ApiJson.MAPPER.readValue(response.body(), CaseFileResponse.class).data());
            case 404 -> Optional.empty();
            default -> throw new IOException("Oficialía answered " + response.statusCode() + ": " + response.body());
        };
    }
}
```

- `OFICIALIA_API_URL` es la URL de producción (o la local) y `OFICIALIA_API_KEY` tu clave: van en las
  variables de entorno de tu sistema, igual que las de la base.
- `@JsonIgnoreProperties(ignoreUnknown = true)` deja que el `record` tome solo los campos que te
  interesan; si Oficialía agrega campos nuevos, tu código no se rompe.
- Jackson convierte solo `receivedDate` a `LocalDate` y `admittedAt` a `LocalDateTime`.
- Llama a `findCaseFile("2026/00431/12")` desde tu servlet o tu servicio.

## 6. Errores

Todos los errores tienen la misma forma:

```json
{ "error": { "code": "CASE_FILE_NOT_FOUND", "message": "No case file has the number 2026/99999/12.", "details": [] } }
```

| Estado | `code` | Qué pasó | Qué hacer |
| --- | --- | --- | --- |
| 401 | `INVALID_CREDENTIALS` | Falta el encabezado `X-Api-Key` o la clave no existe | Revisa la variable de entorno; si sigue, pide la clave otra vez |
| 400 | `INVALID_CASE_FILE_NUMBER` | El número no tiene la forma `AAAA/NNNNN/JJ` | Revisa que lleve las dos diagonales y los ceros a la izquierda |
| 400 | `INVALID_PARAMETER` | `admittedSince` no es una fecha `AAAA-MM-DD` | Manda la fecha en ISO 8601 |
| 404 | `CASE_FILE_NOT_FOUND` | No existe un expediente con ese número | Trátalo como "no encontrado", no como falla |
| 404 | `COURT_NOT_FOUND` | No existe un juzgado con esa clave | Igual que el anterior |
| 503 | (solo en `/health`) | Oficialía responde, pero no alcanza su base | Avisa al equipo de Oficialía |

Usa el `code` para decidir qué hacer y el `message` solo para tus logs: el texto puede cambiar, el
código no.

## Dudas o cambios

Si necesitas un dato que la API no da, pídelo al equipo de Oficialía. Cualquier cambio al contrato
se acuerda primero y se avisa a todos los equipos que lo consumen.
