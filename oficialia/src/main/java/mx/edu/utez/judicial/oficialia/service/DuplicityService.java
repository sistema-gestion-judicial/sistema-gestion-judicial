package mx.edu.utez.judicial.oficialia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.model.ActiveDemand;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;

/**
 * Looks for an active lawsuit in any court with the same RFC or CURP in the same role and the same matter.
 * Active lawsuits are the admissible requests of SCD-2026 plus sample lawsuits of other courts,
 * until the shared database of the 30 courts is available.
 */
public class DuplicityService {

    private static final List<ActiveDemand> OTHER_COURTS = List.of(
            new ActiveDemand("Juzgado de Jojutla", "Familiar", "Primera instancia", "TOSA850312ML4",
                    "PEGL800101HD1"),
            new ActiveDemand("Juzgado de Yautepec", "Civil", "Segunda instancia", "RUHJ900415ABC",
                    "MSU010203KL9"),
            new ActiveDemand("Juzgado de Cuernavaca", "Mercantil", "Primera instancia", "GAV020304QW1",
                    "FSU990101AA2"));

    private final Supplier<List<ActiveDemand>> activeDemands;

    public DuplicityService() {
        this(InMemoryDemandRequestDao.getInstance());
    }

    /** Admissible requests count as active lawsuits as soon as they are ruled (RF 3.5). */
    public DuplicityService(DemandRequestDao demandRequestDao) {
        this.activeDemands = () -> {
            List<ActiveDemand> demands = new ArrayList<>(OTHER_COURTS);
            demandRequestDao.findAll().stream()
                    .filter(request -> request.getStatus() == RequestStatus.ADMISSIBLE)
                    .map(DuplicityService::toActiveDemand)
                    .forEach(demands::add);
            return demands;
        };
    }

    public DuplicityService(List<ActiveDemand> activeDemands) {
        List<ActiveDemand> copy = List.copyOf(activeDemands);
        this.activeDemands = () -> copy;
    }

    public Optional<DuplicityMatch> findMatch(DuplicityCheckForm form) {
        for (ActiveDemand demand : activeDemands.get()) {
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

    private static ActiveDemand toActiveDemand(DemandRequest request) {
        return new ActiveDemand(request.getCourtName(), request.getMatter(), "Primera instancia",
                request.getPlaintiff().getPersonId(), request.getDefendant().getPersonId());
    }
}
