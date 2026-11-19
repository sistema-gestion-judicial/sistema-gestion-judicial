package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.AuditEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AuditDao {
    private final MemoryStore store = MemoryStore.get();

    /** Newest first. */
    public List<AuditEntry> findByAppeal(int appealId) {
        List<AuditEntry> result = new ArrayList<>();
        for (AuditEntry e : store.audit) {
            if (e.getAppealId() == appealId) result.add(e);
        }
        result.sort(Comparator.comparing(AuditEntry::getTimestamp).reversed());
        return result;
    }

    public void add(AuditEntry entry) {
        entry.setId(store.auditSeq.incrementAndGet());
        store.audit.add(entry);
    }
}
