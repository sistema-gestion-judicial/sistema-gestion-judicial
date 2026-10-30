package mx.edu.utez.judicial.oficialia.service;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.PartyRole;

/**
 * Looks for an active lawsuit in any court with the same RFC or CURP in the same role and the same matter.
 * Uses sample data until the shared database of the 30 courts is available.
 */
public class DuplicityService {

    private final List<ActiveDemand> activeDemands;

    public DuplicityService() {
        this(List.of(
                new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "TOSA850312ML4",
                        "PEGL800101HD1"),
                new ActiveDemand("Juzgado de Yautepec", "Civil", "Segunda instancia", "RUHJ900415ABC",
                        "MSU010203KL9"),
                new ActiveDemand("Juzgado de Cuernavaca", "Mercantil", "Primera instancia", "GAV020304QW1",
                        "FSU990101AA2")));
    }

    public DuplicityService(List<ActiveDemand> activeDemands) {
        this.activeDemands = List.copyOf(activeDemands);
    }

    public Optional<DuplicityMatch> findMatch(DuplicityCheckForm form) {
        for (ActiveDemand demand : activeDemands) {
            if (!demand.getMatter().equalsIgnoreCase(form.getMatter())) {
                continue;
            }
            if (!form.isPlaintiffAnonymous() && form.getPlaintiffId().equals(demand.getPlaintiffId())) {
                return Optional.of(new DuplicityMatch(demand, PartyRole.PLAINTIFF));
            }
            if (!form.isDefendantUnknown() && form.getDefendantId().equals(demand.getDefendantId())) {
                return Optional.of(new DuplicityMatch(demand, PartyRole.DEFENDANT));
            }
        }
        return Optional.empty();
    }
}
