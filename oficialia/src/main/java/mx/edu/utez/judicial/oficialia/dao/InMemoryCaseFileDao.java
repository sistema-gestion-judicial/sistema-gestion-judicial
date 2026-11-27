package mx.edu.utez.judicial.oficialia.dao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import mx.edu.utez.judicial.oficialia.model.CaseFileState;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Case file states kept in memory when no database is configured. A case file of the demand requests that
 * has no state yet counts as just opened. Changes live only as long as the server runs.
 */
public class InMemoryCaseFileDao implements CaseFileDao {

    private static final InMemoryCaseFileDao INSTANCE = new InMemoryCaseFileDao(InMemoryDemandRequestDao.getInstance());

    private final DemandRequestDao demandRequests;
    private final Map<String, CaseFileState> states = new ConcurrentHashMap<>();

    public InMemoryCaseFileDao(DemandRequestDao demandRequests) {
        this.demandRequests = demandRequests;
    }

    public static InMemoryCaseFileDao getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized Map<String, CaseFileState> findStates() {
        Map<String, CaseFileState> all = new ConcurrentHashMap<>();
        demandRequests.findAll().stream()
            .filter(request -> request.getStatus() == RequestStatus.ADMISSIBLE)
            .map(DemandRequest::getCaseFileNumber)
            .forEach(number -> all.put(number, states.getOrDefault(number, CaseFileState.opened(number))));
        return all;
    }

    @Override
    public Optional<CaseFileState> findState(String number) {
        return Optional.ofNullable(findStates().get(number));
    }

    @Override
    public synchronized boolean recordFirstInstanceResolution(String number, LocalDateTime resolvedAt) {
        Optional<CaseFileState> current = findState(number);
        current.ifPresent(state -> states.put(number, state.firstInstanceResolvedAt() != null ? state
            : new CaseFileState(number, resolvedAt, state.secondInstance(), state.chamberCode(),
                state.chamberName(), state.secondInstanceAt(), state.concludedAt())));
        return current.isPresent();
    }

    @Override
    public synchronized boolean recordConclusion(String number, LocalDateTime concludedAt) {
        Optional<CaseFileState> current = findState(number);
        current.ifPresent(state -> states.put(number, state.concludedAt() != null ? state
            : new CaseFileState(number, state.firstInstanceResolvedAt(), state.secondInstance(),
                state.chamberCode(), state.chamberName(), state.secondInstanceAt(), concludedAt)));
        return current.isPresent();
    }

    @Override
    public synchronized void moveToSecondInstance(String number, String chamberCode, String chamberName,
                                                  User clerk, LocalDateTime at) {
        CaseFileState state = findState(number).orElseThrow(
            () -> new IllegalStateException("No case file has the number " + number));
        if (state.chamberCode() != null) {
            throw new IllegalStateException("The case file " + number + " already has a chamber");
        }
        LocalDateTime since = state.secondInstance() ? state.secondInstanceAt() : at;
        states.put(number, new CaseFileState(number, state.firstInstanceResolvedAt(), true, chamberCode,
            chamberName, since, state.concludedAt()));
    }
}
