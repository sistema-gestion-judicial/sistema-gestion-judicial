package mx.edu.utez.judicial.oficialia.model;

import java.time.LocalDateTime;

/**
 * A registration the clerk cancelled after a duplicity warning. The request is not saved, but the attempt is,
 * with the demand that was found, who cancelled and when (DFR 3.1).
 */
public record CancelledAttempt(String courtCode, String matchedCourt, String matchedMatter,
                               boolean matchedInSecondInstance, String cancelledBy, LocalDateTime cancelledAt) {

    /** The attempt of cancelling the registration of a request that matched {@code match}. */
    public static CancelledAttempt of(String courtCode, DuplicityMatch match, String cancelledBy,
                                      LocalDateTime cancelledAt) {
        boolean secondInstance = match.getInstance() != null && match.getInstance().startsWith("Segunda");
        return new CancelledAttempt(courtCode, match.getCourtName(), match.getMatter(), secondInstance, cancelledBy,
            cancelledAt);
    }
}
