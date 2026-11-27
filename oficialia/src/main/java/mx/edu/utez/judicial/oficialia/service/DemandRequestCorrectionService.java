package mx.edu.utez.judicial.oficialia.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.DemandRequestDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.DuplicityMatch;
import mx.edu.utez.judicial.oficialia.model.DuplicityWarning;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.PartyRole;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * Corrects a demand request that was not ruled yet (DFR 3.4). Only the Oficial de Partes of its court does it;
 * folio, court, date and annexes stay as they were captured.
 */
public class DemandRequestCorrectionService {

    public static final int MAX_REPRESENTATIVE_LENGTH = 150;
    static final String CONTINUED = "Continuó con la corrección";

    private final DemandRequestDao demandRequestDao;
    private final CatalogDao catalogDao;
    private final DuplicityService duplicityService;
    private final Supplier<LocalDateTime> clock;

    public DemandRequestCorrectionService() {
        this(OficialiaDaos.demandRequests(), OficialiaDaos.catalogs(), new DuplicityService(), LocalDateTime::now);
    }

    public DemandRequestCorrectionService(DemandRequestDao demandRequestDao, CatalogDao catalogDao,
                                          DuplicityService duplicityService, Supplier<LocalDateTime> clock) {
        this.demandRequestDao = demandRequestDao;
        this.catalogDao = catalogDao;
        this.duplicityService = duplicityService;
        this.clock = clock;
    }

    /**
     * The pending request of the clerk's court, to fill the correction form.
     *
     * @throws AccessDeniedException when the user is not an Oficial de Partes
     * @throws NotFoundException when the court has no request with that folio
     * @throws BusinessRuleException when the request was already ruled
     */
    public DemandRequest getForCorrection(User clerk, String folio) {
        if (clerk == null || clerk.getRole() != UserRole.CLERK) {
            throw new AccessDeniedException("Solo el Oficial de Partes corrige solicitudes.");
        }
        DemandRequest request = demandRequestDao.findByFolio(folio)
            .filter(found -> found.getCourtName().equals(clerk.getCourtName()))
            .orElseThrow(() -> new NotFoundException("No existe una solicitud de su juzgado con ese folio."));
        if (!request.isPending()) {
            throw new BusinessRuleException("La solicitud " + request.getFolio() + " ya fue calificada como "
                + request.getStatus().getLabel().toLowerCase() + ", por eso ya no se puede corregir.");
        }
        return request;
    }

    /** The subject matters offered: the active ones, plus the one the request already has. */
    public List<String> matters(DemandRequest request) {
        List<String> names = new ArrayList<>(catalogDao.findSubjectMatters().stream()
            .filter(SubjectMatter::isActive).map(SubjectMatter::getName).toList());
        if (!names.contains(request.getMatter())) {
            names.add(0, request.getMatter());
        }
        return names;
    }

    /**
     * Saves the correction. When a party or the subject matter changed, the duplicity check runs again; a match
     * stops the save until the clerk continues despite the warning, and then the warning is kept with the
     * request. The change is written to the audit log with the clerk and the date.
     *
     * @param continueDespiteWarning whether the clerk already saw the warning and chose to continue
     * @throws AccessDeniedException when the user is not an Oficial de Partes
     * @throws NotFoundException when the court has no request with that folio
     * @throws BusinessRuleException when the request was already ruled
     */
    public Correction correct(User clerk, String folio, DuplicityCheckForm form, String legalRepresentative,
                              boolean continueDespiteWarning) {
        DemandRequest current = getForCorrection(clerk, folio);
        Map<String, String> errors = new LinkedHashMap<>(form.validate());
        String representative = legalRepresentative == null ? "" : legalRepresentative.trim();
        if (representative.length() > MAX_REPRESENTATIVE_LENGTH) {
            errors.put("legalRepresentative", "No puede pasar de " + MAX_REPRESENTATIVE_LENGTH + " caracteres.");
        }
        if (!errors.containsKey("matter") && !matters(current).contains(form.getMatter())) {
            errors.put("matter", "Seleccione una materia activa del catálogo.");
        }
        if (!errors.isEmpty()) {
            return Correction.rejected(errors);
        }
        DemandRequest corrected = new DemandRequest(current.getFolio(), current.getReceivedDate(),
            current.getCourtName(), current.getCourtCode(), form.getMatter(),
            party(PartyRole.PLAINTIFF, form.isPlaintiffAnonymous(), form.getPlaintiffType(), form.getPlaintiffName(),
                form.getPlaintiffId(), current.getPlaintiff()),
            party(PartyRole.DEFENDANT, form.isDefendantUnknown(), form.getDefendantType(), form.getDefendantName(),
                form.getDefendantId(), current.getDefendant()),
            representative.isEmpty() ? null : representative, current.getCapturedBy(), current.getAnnexes(),
            current.getDuplicityWarning());

        DuplicityWarning warning = null;
        if (partiesOrMatterChanged(current, corrected)) {
            Optional<DuplicityMatch> match = duplicityService.findMatch(form);
            if (match.isPresent() && !continueDespiteWarning) {
                return Correction.warned(match.get());
            }
            warning = match.map(found -> new DuplicityWarning(found.getCourtName(), found.getMatter(), CONTINUED))
                .orElse(null);
        }
        try {
            demandRequestDao.correct(corrected, warning, clerk, clock.get());
        } catch (IllegalStateException e) {
            throw new BusinessRuleException("La solicitud " + folio
                + " se calificó mientras la corregía, por eso ya no se puede corregir.");
        }
        return Correction.saved(corrected);
    }

    /** Outcome of a correction: saved, stopped by field errors, or waiting for the clerk to see a warning. */
    public record Correction(DemandRequest request, Map<String, String> errors, DuplicityMatch warning) {

        static Correction saved(DemandRequest request) {
            return new Correction(request, Map.of(), null);
        }

        static Correction rejected(Map<String, String> errors) {
            return new Correction(null, errors, null);
        }

        static Correction warned(DuplicityMatch warning) {
            return new Correction(null, Map.of(), warning);
        }

        public boolean isSaved() {
            return request != null;
        }
    }

    /** Builds the corrected party, keeping the phone and e-mail captured before for the same person. */
    private static Party party(PartyRole role, boolean unidentified, String type, String name, String personId,
                               Party before) {
        if (unidentified) {
            return Party.unidentified(role);
        }
        String personType = "COMPANY".equals(type) ? "Persona moral" : "Persona física";
        boolean samePerson = before.isIdentified() && personId.equals(before.getPersonId());
        return new Party(role, name, personType, personId, samePerson ? before.getPhone() : null,
            samePerson ? before.getEmail() : null);
    }

    private static boolean partiesOrMatterChanged(DemandRequest before, DemandRequest after) {
        return !before.getMatter().equals(after.getMatter())
            || !sameParty(before.getPlaintiff(), after.getPlaintiff())
            || !sameParty(before.getDefendant(), after.getDefendant());
    }

    private static boolean sameParty(Party before, Party after) {
        return Objects.equals(before.getName(), after.getName())
            && Objects.equals(before.getPersonId(), after.getPersonId())
            && Objects.equals(before.getPersonType(), after.getPersonType());
    }
}
