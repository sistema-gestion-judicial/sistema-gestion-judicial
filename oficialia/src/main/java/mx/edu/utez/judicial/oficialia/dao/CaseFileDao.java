package mx.edu.utez.judicial.oficialia.dao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.CaseFileState;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * The state of the case files (case_files, V006 and V009): resolution, second instance and conclusion. The
 * parties and the subject matter stay in the demand request the case file came from.
 */
public interface CaseFileDao {

    /** The state of every case file, by number. */
    Map<String, CaseFileState> findStates();

    Optional<CaseFileState> findState(String number);

    /**
     * Records that the first instance was resolved, as another system reports it. Keeps the first date when
     * it was already recorded.
     *
     * @return false when no case file has the number
     */
    boolean recordFirstInstanceResolution(String number, LocalDateTime resolvedAt);

    /**
     * Records that the case file concluded, as another system reports it. Keeps the first date when it was
     * already recorded.
     *
     * @return false when no case file has the number
     */
    boolean recordConclusion(String number, LocalDateTime concludedAt);

    /**
     * Moves the case file to second instance with the chosen chamber, or with the chamber pending when it is
     * null, and writes it to the audit log with the clerk. When the case file is already in second instance
     * with its chamber pending, only the chamber is filled in.
     *
     * @throws IllegalStateException when the case file already has a chamber
     */
    void moveToSecondInstance(String number, String chamberCode, String chamberName, User clerk, LocalDateTime at);
}
