package mx.edu.utez.judicial.oficialia.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import mx.edu.utez.judicial.oficialia.client.HearingDeskClient;
import mx.edu.utez.judicial.oficialia.client.SampleHearingDeskClient;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;
import mx.edu.utez.judicial.oficialia.model.RulingForm;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Rules a demand request as admissible or inadmissible (RF 3.5). The ruling cannot be undone.
 */
public class RulingService {

    private static final HearingDeskClient SHARED_DESK_CLIENT = new SampleHearingDeskClient();

    private final DemandRequestDao demandRequestDao;
    private final DemandRequestService demandRequestService;
    private final CaseFileNumberGenerator caseFileNumberGenerator;
    private final HearingDeskClient hearingDeskClient;
    private final Clock clock;

    public RulingService() {
        this(InMemoryDemandRequestDao.getInstance(), SHARED_DESK_CLIENT, Clock.systemDefaultZone());
    }

    public RulingService(DemandRequestDao demandRequestDao, HearingDeskClient hearingDeskClient, Clock clock) {
        this.demandRequestDao = demandRequestDao;
        this.demandRequestService = new DemandRequestService(demandRequestDao);
        this.caseFileNumberGenerator = new CaseFileNumberGenerator(demandRequestDao);
        this.hearingDeskClient = hearingDeskClient;
        this.clock = clock;
    }

    /**
     * Returns a request the user may rule: from their court, still pending, and the user is the clerk.
     *
     * @throws AccessDeniedException when the user is not the clerk or the request is from another court
     * @throws BusinessRuleException when the request was already ruled
     */
    public DemandRequest getRulable(String folio, User user) {
        if (!user.isClerk()) {
            throw new AccessDeniedException("Solo la Oficial de Partes puede calificar solicitudes.");
        }
        DemandRequest request = demandRequestService.getForCourt(folio, user.getCourtName());
        if (!request.isPending()) {
            throw new BusinessRuleException("La solicitud " + request.getFolio()
                    + " ya fue calificada como " + request.getStatus().getLabel().toLowerCase(Locale.ROOT)
                    + " y su estado no puede cambiar.");
        }
        return request;
    }

    /**
     * Applies a valid ruling. An admissible request gets its case file number and, when the Audiencias
     * system answers, its desk; otherwise the desk stays pending.
     */
    public synchronized DemandRequest rule(String folio, User user, RulingForm form) {
        DemandRequest request = getRulable(folio, user);
        LocalDateTime now = LocalDateTime.now(clock);
        if (form.getDecision() == RequestStatus.ADMISSIBLE) {
            String caseFileNumber = caseFileNumberGenerator.next(request.getCourtCode(), now.getYear());
            request.recordAdmissible(caseFileNumber, askForDesk(caseFileNumber, request.getMatter()),
                    user.getFullName(), now);
        } else {
            request.recordInadmissible(form.getReason(), user.getFullName(), now);
        }
        demandRequestDao.update(request);
        return request;
    }

    private String askForDesk(String caseFileNumber, String matter) {
        try {
            return hearingDeskClient.assignDesk(caseFileNumber, matter);
        } catch (ServiceUnavailableException e) {
            return null;
        }
    }
}
