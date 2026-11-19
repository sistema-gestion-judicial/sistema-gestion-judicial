package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Resolution;

public class ResolutionDao {
    private final MemoryStore store = MemoryStore.get();

    public Resolution findByAppeal(int appealId) {
        return store.resolutions.get(appealId);
    }

    public void save(Resolution resolution) {
        store.resolutions.put(resolution.getAppealId(), resolution);
    }
}
