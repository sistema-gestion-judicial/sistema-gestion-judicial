package mx.edu.utez.judicial.common.dao;

import mx.edu.utez.judicial.tocas.model.User;

public class UserDao {
    private final MemoryStore store = MemoryStore.get();

    public User findByEmail(String email) {
        for (User u : store.users.values()) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    public User findById(int id) {
        return store.users.get(id);
    }
}
