package mx.edu.utez.judicial.audiencias.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AudienciasMigrationTest {

    @Test
    void migrationScriptExistsOnClasspathAndIsNotEmpty() throws Exception {
        String path = "db/migration/V001__create_audiencias_tables.sql";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "El script de migracion no se encontro en el classpath: " + path);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertFalse(content.isBlank(), "El script de migracion esta vacio");
        }
    }
}
