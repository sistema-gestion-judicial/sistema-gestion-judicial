package mx.edu.utez.judicial.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiJsonTest {

    @Test
    void writesDatesInIso8601AndKeepsNulls() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("receivedDate", LocalDate.parse("2026-10-23"));
        body.put("admittedAt", LocalDateTime.parse("2026-10-24T10:15"));
        body.put("hearingDesk", null);

        assertEquals("{\"receivedDate\":\"2026-10-23\",\"admittedAt\":\"2026-10-24T10:15:00\",\"hearingDesk\":null}",
            ApiJson.write(body));
    }

    @Test
    void wrapsErrorsInTheSharedFormat() {
        assertEquals("{\"error\":{\"code\":\"CASE_FILE_NOT_FOUND\",\"message\":\"No case file\",\"details\":[]}}",
            ApiJson.write(new ApiError("CASE_FILE_NOT_FOUND", "No case file", List.of())));
    }
}
