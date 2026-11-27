package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserStatusFormTest {

    private static UserStatusForm form(String status, String reason) {
        UserStatusForm form = new UserStatusForm();
        form.setStatus(status);
        form.setReason(reason);
        return form;
    }

    @Test
    void acceptsBothStatusesWithoutReason() {
        assertTrue(form("active", null).validate().isEmpty());
        assertTrue(form("active", null).isActivating());
        assertTrue(form(" inactive ", "").validate().isEmpty());
        assertFalse(form("inactive", "").isActivating());
        assertNull(form("inactive", "   ").getReasonOrNull());
    }

    @Test
    void requiresTheNewStatus() {
        assertEquals("Elija el nuevo estado de la cuenta.", form(null, null).validate().get("status"));
        assertEquals("Elija el nuevo estado de la cuenta.", form("deleted", null).validate().get("status"));
    }

    @Test
    void keepsTheReasonTrimmedAndWithinTheSizeOfTheAuditLog() {
        assertEquals("Cambió de área", form("inactive", "  Cambió de área ").getReasonOrNull());
        assertTrue(form("inactive", "x".repeat(UserStatusForm.MAX_REASON_LENGTH)).validate().isEmpty());
        assertEquals("El motivo no puede pasar de 500 caracteres.",
            form("inactive", "x".repeat(UserStatusForm.MAX_REASON_LENGTH + 1)).validate().get("reason"));
    }
}
