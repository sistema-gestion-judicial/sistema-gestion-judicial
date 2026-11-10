package mx.edu.utez.judicial.common.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class SqlMigratorTest {

    @Test
    void splitsCommandsAndSkipsComments() {
        String sql = """
                -- Catalogs
                CREATE TABLE courts (
                  code CHAR(2) PRIMARY KEY
                );

                INSERT INTO courts (code) VALUES ('12');
                """;

        assertEquals(List.of("CREATE TABLE courts (\n  code CHAR(2) PRIMARY KEY\n)",
            "INSERT INTO courts (code) VALUES ('12')"), SqlMigrator.split(sql));
    }
}
