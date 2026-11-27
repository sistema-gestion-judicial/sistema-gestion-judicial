package mx.edu.utez.judicial.audiencias.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import mx.edu.utez.judicial.audiencias.dao.desk.DeskDao;
import mx.edu.utez.judicial.audiencias.dao.user.UserDao;
import mx.edu.utez.judicial.audiencias.model.Desk;
import mx.edu.utez.judicial.audiencias.model.user.Role;
import mx.edu.utez.judicial.audiencias.model.user.User;

/**
 * Business rules for desks. Every message thrown as IllegalArgumentException is written for the
 * person using the form, so the controller can show it as is.
 */
public class DeskService {

    private static final int MAX_DESK_NAME = 50;   // desks.desk_name VARCHAR(50)
    private static final int MAX_MATTER = 30;      // desks.matter VARCHAR(30)
    private static final Pattern COURT_CODE = Pattern.compile("^\\d{2}$");

    /** An active lawyer and the desk that already has him or her, if any. */
    public record LawyerOption(int id, String fullName, Integer deskId) {
    }

    private final DeskDao deskDao;
    private final UserDao userDao;

    public DeskService(DeskDao deskDao, UserDao userDao) {
        this.deskDao = deskDao;
        this.userDao = userDao;
    }

    public List<Desk> findAll(String search, String matter) {
        String cleanSearch = search == null ? "" : search.trim().toLowerCase();
        String cleanMatter = matter == null ? "" : matter.trim();

        return deskDao.findAll().stream()
            .filter(d -> cleanSearch.isEmpty()
                || (d.getDeskName() != null && d.getDeskName().toLowerCase().contains(cleanSearch))
                || (d.getCourtId() != null && d.getCourtId().toLowerCase().contains(cleanSearch)))
            .filter(d -> cleanMatter.isEmpty()
                || (d.getMatter() != null && d.getMatter().equalsIgnoreCase(cleanMatter)))
            .toList();
    }

    public Optional<Desk> findById(int id) {
        return deskDao.findById(id);
    }

    /** Active lawyers by name, each with the desk that already has them (null when free). */
    public List<LawyerOption> findLawyerOptions() {
        Map<Integer, Integer> deskByLawyer = new HashMap<>();
        for (Desk desk : deskDao.findAll()) {
            if (desk.getUserId() != null) {
                deskByLawyer.putIfAbsent(desk.getUserId(), desk.getId());
            }
        }
        // findActiveLawyers joins desks, so index by id to be sure each lawyer appears once
        Map<Integer, LawyerOption> options = new LinkedHashMap<>();
        for (User lawyer : userDao.findActiveLawyers(Role.LAWYER)) {
            if (lawyer.getId() != null) {
                options.putIfAbsent(lawyer.getId(),
                    new LawyerOption(lawyer.getId(), lawyer.getFullName(), deskByLawyer.get(lawyer.getId())));
            }
        }
        List<LawyerOption> sorted = new ArrayList<>(options.values());
        sorted.sort(Comparator.comparing(
            (LawyerOption o) -> o.fullName() == null ? "" : o.fullName(), String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    public int createDesk(String deskName, String matter, boolean active, Integer userId, String courtId) {
        Desk desk = validated(null, deskName, matter, active, userId, courtId);
        validateLawyer(userId, null);
        return deskDao.insert(desk);
    }

    public void updateDesk(int id, String deskName, String matter, boolean active, Integer userId,
                           String courtId) {
        if (deskDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("La mesa especificada no existe.");
        }
        Desk desk = validated(id, deskName, matter, active, userId, courtId);
        validateLawyer(userId, id);
        deskDao.update(desk);
    }

    public void setActive(int id, boolean active) {
        if (deskDao.findById(id).isEmpty()) {
            throw new IllegalArgumentException("La mesa especificada no existe.");
        }
        deskDao.setActive(id, active);
    }

    private Desk validated(Integer id, String deskName, String matter, boolean active, Integer userId,
                           String courtId) {
        String name = clean(deskName);
        String cleanMatter = clean(matter);
        String court = clean(courtId);

        if (name == null) {
            throw new IllegalArgumentException("El número o nombre de la mesa es obligatorio.");
        }
        if (name.length() > MAX_DESK_NAME) {
            throw new IllegalArgumentException(
                "El nombre de la mesa admite máximo " + MAX_DESK_NAME + " caracteres.");
        }
        if (cleanMatter == null) {
            throw new IllegalArgumentException("La materia jurídica es obligatoria.");
        }
        if (cleanMatter.length() > MAX_MATTER) {
            throw new IllegalArgumentException(
                "La materia admite máximo " + MAX_MATTER + " caracteres.");
        }
        if (court == null) {
            throw new IllegalArgumentException("El juzgado asignado es obligatorio.");
        }
        if (!COURT_CODE.matcher(court).matches()) {
            throw new IllegalArgumentException("La clave del juzgado no es válida.");
        }
        return new Desk(id, name, cleanMatter, active, userId, court);
    }

    private void validateLawyer(Integer userId, Integer currentDeskId) {
        if (userId == null) {
            return;
        }
        boolean activeLawyer = findLawyerOptions().stream().anyMatch(l -> l.id() == userId);
        if (!activeLawyer) {
            throw new IllegalArgumentException("El abogado seleccionado no existe o no está activo.");
        }
        Optional<Desk> existing = deskDao.findByUserId(userId);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), currentDeskId)) {
            throw new IllegalArgumentException("El abogado seleccionado ya está asignado a otra mesa.");
        }
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
