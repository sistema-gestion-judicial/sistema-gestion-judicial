package mx.edu.utez.judicial.oficialia.model;

/**
 * A chamber (sala) of the Tocas system that can take a case file in second instance (DFR 3.7).
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class Chamber {

    private final String code;
    private final String name;
    private final String seat;

    public Chamber(String code, String name, String seat) {
        this.code = code;
        this.name = name;
        this.seat = seat;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSeat() {
        return seat;
    }
}
