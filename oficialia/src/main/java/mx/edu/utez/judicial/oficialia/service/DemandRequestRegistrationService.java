package mx.edu.utez.judicial.oficialia.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.RequestDetailsForm;
import mx.edu.utez.judicial.oficialia.model.User;

/**
 * Registers a demand request after its duplicity check (DFR 3.1, step 2). The check runs again on save, so a
 * request is never saved without it, and a match is only kept when the clerk chose to continue.
 */
public class DemandRequestRegistrationService {

    static final String CONTINUED = "Continuó con el registro";

    private final DemandRequestDao demandRequestDao;
    private final CatalogDao catalogDao;
    private final DuplicityService duplicityService;
    private final SubjectMatterService subjectMatterService;
    private final Supplier<LocalDateTime> clock;

    public DemandRequestRegistrationService() {
        this(OficialiaDaos.demandRequests(), OficialiaDaos.catalogs(), new DuplicityService(), LocalDateTime::now);
    }

    public DemandRequestRegistrationService(DemandRequestDao demandRequestDao, CatalogDao catalogDao,
                                            DuplicityService duplicityService, Supplier<LocalDateTime> clock) {
        this.demandRequestDao = demandRequestDao;
        this.catalogDao = catalogDao;
        this.duplicityService = duplicityService;
        this.subjectMatterService = new SubjectMatterService(catalogDao);
        this.clock = clock;
    }

    /**
     * Saves the request as pending with the next folio. Nothing is saved when a field is missing, when the
     * check finds a match the clerk did not accept, or when no annex was attached.
     *
     * @param warningAccepted whether the clerk saw the duplicity warning and chose to continue
     * @throws AccessDeniedException when the user is not staff of a court
     */
    public Registration register(User clerk, DuplicityCheckForm parties, RequestDetailsForm details,
                                 boolean warningAccepted) {
        Court court = courtOf(clerk);
        Map<String, String> errors = new LinkedHashMap<>(parties.validate());
        if (!errors.containsKey("matter") && !subjectMatterService.activeNames().contains(parties.getMatter())) {
            errors.put("matter", "Seleccione una materia activa del catálogo.");
        }
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        Optional<DuplicityMatch> match = duplicityService.findMatch(parties);
        if (match.isPresent() && !warningAccepted) {
            return Registration.warned(match.get());
        }
        errors.putAll(details.validate(!parties.isPlaintiffAnonymous(), !parties.isDefendantUnknown()));
        if (!errors.isEmpty()) {
            return Registration.rejected(errors);
        }
        LocalDateTime now = clock.get();
        DuplicityWarning warning = match
            .map(found -> new DuplicityWarning(found.getCourtName(), found.getMatter(), CONTINUED))
            .orElse(null);
        DemandRequest draft = new DemandRequest("", now.toLocalDate(), court.getName(), court.getCode(),
            parties.getMatter(),
            details.plaintiff(party(PartyRole.PLAINTIFF, parties.isPlaintiffAnonymous(), parties.getPlaintiffType(),
                parties.getPlaintiffName(), parties.getPlaintiffId())),
            details.defendant(party(PartyRole.DEFENDANT, parties.isDefendantUnknown(), parties.getDefendantType(),
                parties.getDefendantName(), parties.getDefendantId())),
            details.getLegalRepresentativeOrNull(), clerk.getFullName(), details.toAnnexes(), warning);
        draft.setFilingType(details.getFilingTypeValue());
        return Registration.saved(demandRequestDao.register(draft, clerk, now));
    }

    /**
     * Keeps the attempt when the clerk cancels after a duplicity warning (DFR 3.1). Without a match there is
     * nothing to keep: the clerk simply left the form.
     */
    public void cancel(User clerk, DuplicityCheckForm parties) {
        Court court = courtOf(clerk);
        if (!parties.validate().isEmpty()) {
            return;
        }
        duplicityService.findMatch(parties).ifPresent(match -> demandRequestDao.recordCancelledAttempt(
            court.getCode(), match.getCourtName(), match.getMatter(), clerk, clock.get()));
    }

    /** Outcome of a registration: the folio, the field errors, or a warning the clerk has to see first. */
    public record Registration(String folio, Map<String, String> errors, DuplicityMatch warning) {

        static Registration saved(String folio) {
            return new Registration(folio, Map.of(), null);
        }

        static Registration rejected(Map<String, String> errors) {
            return new Registration(null, errors, null);
        }

        static Registration warned(DuplicityMatch warning) {
            return new Registration(null, Map.of(), warning);
        }

        public boolean isSaved() {
            return folio != null;
        }
    }

    private Court courtOf(User clerk) {
        if (clerk == null || clerk.getCourtName() == null) {
            throw new AccessDeniedException("Solo el personal de un juzgado registra solicitudes.");
        }
        return catalogDao.findCourts().stream()
            .filter(court -> court.getName().equals(clerk.getCourtName()))
            .findFirst()
            .orElseThrow(() -> new AccessDeniedException("No se encontró el juzgado de su cuenta."));
    }

    private static Party party(PartyRole role, boolean unidentified, String type, String name, String personId) {
        if (unidentified) {
            return Party.unidentified(role);
        }
        return new Party(role, name, "COMPANY".equals(type) ? "Persona moral" : "Persona física", personId, null,
            null);
    }
}
