package mx.edu.utez.judicial.oficialia.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.ComplementaryDataForm;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestFolio;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Registers a demand request in two steps: the duplicity check and the complementary data (DFR 3.1).
 * Only an Oficial de Partes registers, always in their own court and with the status pending.
 */
public class DemandRequestRegistrationService {

    static final String INACTIVE_MATTER = "Seleccione una materia activa.";

    private final CatalogDao catalogDao;
    private final DemandRequestDao demandRequestDao;
    private final DuplicityService duplicityService;
    private final Clock clock;

    public DemandRequestRegistrationService() {
        this(OficialiaDaos.catalogs(), OficialiaDaos.demandRequests(), Clock.systemDefaultZone());
    }

    public DemandRequestRegistrationService(CatalogDao catalogDao, DemandRequestDao demandRequestDao, Clock clock) {
        this(catalogDao, demandRequestDao, new DuplicityService(demandRequestDao), clock);
    }

    public DemandRequestRegistrationService(CatalogDao catalogDao, DemandRequestDao demandRequestDao,
                                            DuplicityService duplicityService, Clock clock) {
        this.catalogDao = catalogDao;
        this.demandRequestDao = demandRequestDao;
        this.duplicityService = duplicityService;
        this.clock = clock;
    }

    /** The names of the active subject matters, ordered by name: an inactive one cannot be chosen (DFR 2.5). */
    public List<String> activeMatterNames() {
        return catalogDao.findSubjectMatters().stream()
            .filter(SubjectMatter::isActive)
            .map(SubjectMatter::getName)
            .sorted(Comparator.comparing(CatalogNames::key))
            .toList();
    }

    /**
     * Registers the request with a new folio and the status pending. Nothing is saved when either step has
     * errors.
     *
     * @throws AccessDeniedException when the user is not an Oficial de Partes
     */
    public Registration register(User user, DuplicityCheckForm parties, ComplementaryDataForm data) {
        Court court = courtOf(user);
        Map<String, String> errors = new LinkedHashMap<>(parties.validate());
        if (!errors.containsKey("matter") && !activeMatterNames().contains(parties.getMatter())) {
            errors.put("matter", INACTIVE_MATTER);
        }
        if (errors.containsKey("parties")) {
            return Registration.rejected(errors);
        }
        errors.putAll(data.validate(!parties.isPlaintiffAnonymous(), !parties.isDefendantUnknown()));
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        LocalDate today = LocalDate.now(clock);
        String folio = RequestFolio.of(demandRequestDao.lastFolioNumber(today.getYear()) + 1, today.getYear());
        DemandRequest request = new DemandRequest(folio, today, court.getName(), court.getCode(),
            parties.getMatter(), plaintiff(parties, data), defendant(parties, data),
            data.getLegalRepresentative(), user.getFullName(), data.getAnnexes(), null,
            data.getPresentationTypeValue(), data.observationsOrNull());
        demandRequestDao.insert(request);
        return Registration.saved(request);
    }

    private Court courtOf(User user) {
        if (user == null || user.getRole() != UserRole.CLERK) {
            throw new AccessDeniedException("Solo el Oficial de Partes puede registrar solicitudes de demanda.");
        }
        return catalogDao.findCourts().stream()
            .filter(court -> court.getName().equals(user.getCourtName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("The court of " + user.getEmail() + " is unknown"));
    }

    private static Party plaintiff(DuplicityCheckForm parties, ComplementaryDataForm data) {
        if (parties.isPlaintiffAnonymous()) {
            return Party.unidentified(PartyRole.PLAINTIFF);
        }
        return new Party(PartyRole.PLAINTIFF, parties.getPlaintiffName(), personType(parties.getPlaintiffType()),
            parties.getPlaintiffId(), data.getPlaintiff().getPhoneDigits(), data.getPlaintiff().emailOrNull(),
            data.plaintiffAddress());
    }

    private static Party defendant(DuplicityCheckForm parties, ComplementaryDataForm data) {
        if (parties.isDefendantUnknown()) {
            return Party.unidentified(PartyRole.DEFENDANT);
        }
        return new Party(PartyRole.DEFENDANT, parties.getDefendantName(), personType(parties.getDefendantType()),
            parties.getDefendantId(), data.getDefendant().getPhoneDigits(), data.getDefendant().emailOrNull(),
            data.defendantAddress());
    }

    /** Stored as the requests already saved show it. */
    private static String personType(String type) {
        return "COMPANY".equals(type) ? "Persona moral" : "Persona física";
    }

    /** Outcome of a registration: the saved request, or the field errors that stopped it. */
    public record Registration(DemandRequest request, Map<String, String> errors) {

        static Registration saved(DemandRequest request) {
            return new Registration(request, Map.of());
        }

        static Registration rejected(Map<String, String> errors) {
            return new Registration(null, Collections.unmodifiableMap(new LinkedHashMap<>(errors)));
        }

        public boolean isSaved() {
            return request != null;
        }
    }
}
