package mx.edu.utez.judicial.oficialia.model;

/**
 * Model for MATERIA (Subject matter) of a lawsuit (civil, family, commercial, criminal). Every case file keeps one.
 */
public class SubjectMatter {

    private final int id;
    private final String name;
    private final boolean active;

    public SubjectMatter(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
