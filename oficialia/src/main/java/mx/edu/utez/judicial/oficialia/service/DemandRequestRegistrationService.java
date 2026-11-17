package mx.edu.utez.judicial.oficialia.service;

import java.util.Comparator;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Registers a demand request in two steps: the duplicity check and the complementary data (DFR 3.1).
 */
public class DemandRequestRegistrationService {

    private final CatalogDao catalogDao;

    public DemandRequestRegistrationService() {
        this(OficialiaDaos.catalogs());
    }

    public DemandRequestRegistrationService(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /** The names of the active subject matters, ordered by name: an inactive one cannot be chosen (DFR 2.5). */
    public List<String> activeMatterNames() {
        return catalogDao.findSubjectMatters().stream()
            .filter(SubjectMatter::isActive)
            .map(SubjectMatter::getName)
            .sorted(Comparator.comparing(CatalogNames::key))
            .toList();
    }
}
