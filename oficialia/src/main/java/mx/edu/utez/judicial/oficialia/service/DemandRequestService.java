package mx.edu.utez.judicial.oficialia.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.RequestStatus;

/**
 * Queries the demand requests of the user's court.
 * Returns sample data until DemandRequestDao exists.
 */
public class DemandRequestService {

    private static final List<DemandRequest> SAMPLE_REQUESTS = List.of(
            request("S-000127/2026", "2026-10-26", "Familiar", "Ana Torres", "Luis Torres", null,
                    RequestStatus.PENDING),
            request("S-000126/2026", "2026-10-26", "Civil", null, "Minera del Sur", null,
                    RequestStatus.PENDING),
            request("S-000125/2026", "2026-10-23", "Mercantil", "Grupo Ávila", "Ferre Sur", null,
                    RequestStatus.PENDING),
            request("S-000124/2026", "2026-10-23", "Penal", "Juan Ruiz", null, "2026/00431/12",
                    RequestStatus.ADMISSIBLE),
            request("S-000123/2026", "2026-10-22", "Familiar", "María Gómez", "Raúl Ortiz", null,
                    RequestStatus.INADMISSIBLE),
            request("S-000122/2026", "2026-10-21", "Civil", "Rosa León", "Hugo Vega", "2026/00428/12",
                    RequestStatus.ADMISSIBLE),
            request("S-000121/2026", "2026-10-20", "Mercantil", "Ana Lara", "Banco del Sur", "2026/00425/12",
                    RequestStatus.ADMISSIBLE),
            request("S-000120/2026", "2026-10-20", "Familiar", "Carla Díaz", "Iván Ibarra", null,
                    RequestStatus.INADMISSIBLE),
            request("S-000119/2026", "2026-10-19", "Civil", "Julio Paz", "Inés Ríos", "2026/00419/12",
                    RequestStatus.ADMISSIBLE));

    /**
     * Finds requests whose folio or parties contain the text and that have the given status.
     * Blank filters are ignored. Results are sorted from newest to oldest.
     */
    public List<DemandRequest> search(String text, RequestStatus status) {
        String needle = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        return SAMPLE_REQUESTS.stream()
                .filter(request -> status == null || request.getStatus() == status)
                .filter(request -> needle.isEmpty()
                        || request.getFolio().toLowerCase(Locale.ROOT).contains(needle)
                        || request.getPartiesText().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(DemandRequest::getFolio).reversed())
                .toList();
    }

    public int countAll() {
        return SAMPLE_REQUESTS.size();
    }

    private static DemandRequest request(String folio, String date, String matter, String plaintiff,
            String defendant, String caseFile, RequestStatus status) {
        return new DemandRequest(folio, LocalDate.parse(date), matter, plaintiff, defendant, caseFile, status);
    }
}
