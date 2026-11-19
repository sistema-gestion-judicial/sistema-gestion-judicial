package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Appeal;

import java.util.ArrayList;
import java.util.List;

public class AppealDao {
    private final MemoryStore store = MemoryStore.get();

    public List<Appeal> findByOwner(int ownerId) {
        List<Appeal> result = new ArrayList<>();
        for (Appeal a : store.appeals.values()) {
            if (a.getOwnerId() == ownerId) result.add(a);
        }
        return result;
    }

    public Appeal findById(int id) {
        return store.appeals.get(id);
    }

    public void update(Appeal appeal) {
        store.appeals.put(appeal.getId(), appeal);
    }
}
