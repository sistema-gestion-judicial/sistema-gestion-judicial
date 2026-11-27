package mx.edu.utez.judicial.oficialia.model;

import java.time.LocalDateTime;

/**
 * What the case_files table knows about a case file beyond its demand request: when the first instance was
 * resolved, whether it went to second instance and to which chamber, and when it concluded.
 *
 * @param firstInstanceResolvedAt null while the first instance has no resolution
 * @param secondInstance whether the case file went to second instance
 * @param chamberCode null in first instance, or while the Tocas system has not given a chamber
 * @param concludedAt null while the case file is open
 */
public record CaseFileState(String number, LocalDateTime firstInstanceResolvedAt, boolean secondInstance,
                            String chamberCode, String chamberName, LocalDateTime secondInstanceAt,
                            LocalDateTime concludedAt) {

    /** A case file just opened: first instance, nothing resolved. */
    public static CaseFileState opened(String number) {
        return new CaseFileState(number, null, false, null, null, null, null);
    }

    public CaseFileStatus status() {
        if (concludedAt != null) {
            return CaseFileStatus.CONCLUDED;
        }
        return secondInstance ? CaseFileStatus.SECOND_INSTANCE : CaseFileStatus.ACTIVE;
    }
}
