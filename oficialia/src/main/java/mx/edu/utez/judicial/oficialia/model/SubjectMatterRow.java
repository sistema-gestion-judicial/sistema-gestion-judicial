package mx.edu.utez.judicial.oficialia.model;

/**
 * A row of the subject matters catalog (DFR 2.6): the subject matter and how many courts handle it.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class SubjectMatterRow {

    private final SubjectMatter matter;
    private final int courtCount;

    public SubjectMatterRow(SubjectMatter matter, int courtCount) {
        this.matter = matter;
        this.courtCount = courtCount;
    }

    public String getCode() {
        return matter.getCode();
    }

    public String getName() {
        return matter.getName();
    }

    public String getDescription() {
        return matter.getDescription();
    }

    public boolean isActive() {
        return matter.isActive();
    }

    public int getCourtCount() {
        return courtCount;
    }
}
