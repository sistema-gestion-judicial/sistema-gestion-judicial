package mx.edu.utez.judicial.oficialia.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryDemandRequestDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;

/**
 * Queries the demand requests of the user's court (RF 3.2 and 3.3).
 * A user only sees the requests of their own court.
 */
public class DemandRequestService {

    private final DemandRequestDao demandRequestDao;

    public DemandRequestService() {
        this(InMemoryDemandRequestDao.getInstance());
    }

    public DemandRequestService(DemandRequestDao demandRequestDao) {
        this.demandRequestDao = demandRequestDao;
    }

    /**
     * Finds the court's requests whose folio or parties contain the text and that have the given status.
     * Blank filters are ignored. Results are sorted from newest to oldest.
     */
    public List<DemandRequest> search(String courtName, String text, RequestStatus status) {
        String needle = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        return demandRequestDao.findByCourt(courtName).stream()
                .filter(request -> status == null || request.getStatus() == status)
                .filter(request -> needle.isEmpty()
                        || request.getFolio().toLowerCase(Locale.ROOT).contains(needle)
                        || request.getPartiesText().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(DemandRequest::getFolio).reversed())
                .toList();
    }

    public int countByCourt(String courtName) {
        return demandRequestDao.findByCourt(courtName).size();
    }

    /**
     * Returns a request of the user's court.
     *
     * @throws NotFoundException when no request has that folio
     * @throws AccessDeniedException when the request belongs to another court
     */
    public DemandRequest getForCourt(String folio, String courtName) {
        DemandRequest request = demandRequestDao.findByFolio(folio)
                .orElseThrow(() -> new NotFoundException("No existe una solicitud con el folio indicado."));
        if (!request.getCourtName().equals(courtName)) {
            throw new AccessDeniedException(
                    "No tiene permiso para consultar solicitudes de otro juzgado.");
        }
        return request;
    }
}
