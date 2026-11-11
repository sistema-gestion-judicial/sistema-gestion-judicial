package mx.edu.utez.judicial.oficialia.model;

/**
 * Model for MATERIA (Subject matter) of a lawsuit (civil, family, commercial, criminal). Every case file keeps one.
 * The code (CIV, FAM, MER, PEN) is the stable key the other systems use (DFR 2.5, 3.3.6): the name can change,
 * the code cannot.
 */
public class SubjectMatter {

    private final int id;
    private final String code;
    private final String name;
    private final String description;
    private final boolean active;

    public SubjectMatter(int id, String code, String name, String description, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
