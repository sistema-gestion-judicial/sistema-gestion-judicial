package mx.edu.utez.judicial.tocas.model;

import java.time.LocalDateTime;

/** One annotation of the hearing record ("Registro de lo ocurrido"). Annotations cannot be deleted. */
public class HearingEntry {
    private int id;
    private int hearingId;
    private LocalDateTime timestamp;
    private String type;
    private String text;
    private boolean edited;

    public HearingEntry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHearingId() { return hearingId; }
    public void setHearingId(int hearingId) { this.hearingId = hearingId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public String getTypeLabel() {
        return switch (type == null ? "" : type) {
            case "ATTENDANCE" -> "Asistencia";
            case "EXPOSITION" -> "Exposición";
            case "EVIDENCE" -> "Pruebas";
            case "AGREEMENT" -> "Acuerdo";
            default -> "Observación";
        };
    }
}
