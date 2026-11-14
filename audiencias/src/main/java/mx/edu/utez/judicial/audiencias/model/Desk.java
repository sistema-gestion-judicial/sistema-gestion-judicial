package mx.edu.utez.judicial.audiencias.model;
public class Desk {

    private final Integer id;
    private final String deskName;
    private final String matter;
    private final boolean active;
    private final Integer userId;
    private final String courtId;

    public Desk(String deskName, String matter, boolean active, Integer userId, String courtId) {
        this(null, deskName, matter, active, userId, courtId);
    }

    public Desk(Integer id, String deskName, String matter, boolean active, Integer userId,
                String courtId) {
        this.id = id;
        this.deskName = deskName;
        this.matter = matter;
        this.active = active;
        this.userId = userId;
        this.courtId = courtId;
    }

    public Integer getId() {
        return id;
    }

    public String getDeskName() {
        return deskName;
    }

    public String getMatter() {
        return matter;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getCourtId() {
        return courtId;
    }

    public boolean hasAssignedLawyer() {
        return userId != null;
    }
}
