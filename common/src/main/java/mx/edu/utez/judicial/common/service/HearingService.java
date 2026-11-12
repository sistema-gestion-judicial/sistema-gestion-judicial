package mx.edu.utez.judicial.common.service;

import mx.edu.utez.judicial.tocas.dao.AuditDao;
import mx.edu.utez.judicial.tocas.dao.HearingDao;
import mx.edu.utez.judicial.tocas.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Hearings ("ponencias"): listing, scheduling and the agenda calendar. */
public class HearingService {
    private static final Set<String> TYPES = Set.of("FIRST", "SECOND", "THIRD", "ADDITIONAL");
    private static final LocalTime[] SLOTS = {
        LocalTime.of(10, 0), LocalTime.of(11, 30), LocalTime.of(9, 0), LocalTime.of(12, 30), LocalTime.of(13, 30)
    };
    private static final int MAX_PER_DAY = 3;

    private final HearingDao hearingDao = new HearingDao();
    private final AuditDao auditDao = new AuditDao();
    private final AppealService appealService = new AppealService();
    private final NotificationService notificationService = new NotificationService();

    /** Suggested date/time/type for a new hearing. */
    public static class Proposal {
        private final LocalDate date;
        private final LocalTime time;
        private final String type;

        public Proposal(LocalDate date, LocalTime time, String type) {
            this.date = date;
            this.time = time;
            this.type = type;
        }

        public LocalDate getDate() { return date; }
        public LocalTime getTime() { return time; }
        public String getType() { return type; }
    }

    private void refresh(Hearing h) {
        if ("UPCOMING".equals(h.getStatus()) && h.getDate().isBefore(AppClock.today())) {
            h.setStatus("DONE");
        }
    }

    public List<Hearing> findByAppeal(User user, int appealId) {
        appealService.getOwned(user, appealId);
        List<Hearing> list = new ArrayList<>(hearingDao.findByAppeal(appealId));
        list.forEach(this::refresh);
        list.sort(Comparator.comparing(Hearing::getDate).thenComparing(Hearing::getTime));
        return list;
    }

    public Hearing nextUpcoming(List<Hearing> hearings) {
        LocalDate today = AppClock.today();
        Hearing best = null;
        for (Hearing h : hearings) {
            if (h.isUpcoming() && !h.getDate().isBefore(today) && (best == null || h.getDate().isBefore(best.getDate()))) {
                best = h;
            }
        }
        return best;
    }

    private List<Hearing> byJudge(User user) {
        List<Hearing> list = new ArrayList<>(hearingDao.findByJudge(user.getId()));
        list.forEach(this::refresh);
        list.sort(Comparator.comparing(Hearing::getDate).thenComparing(Hearing::getTime));
        return list;
    }

    public List<Hearing> upcoming(User user, int limit) {
        LocalDate today = AppClock.today();
        List<Hearing> result = new ArrayList<>();
        for (Hearing h : byJudge(user)) {
            if (h.isUpcoming() && !h.getDate().isBefore(today)) result.add(h);
            if (result.size() == limit) break;
        }
        return result;
    }

    public int countWithinDays(User user, int days) {
        LocalDate today = AppClock.today();
        LocalDate end = today.plusDays(days);
        int count = 0;
        for (Hearing h : byJudge(user)) {
            if (h.isUpcoming() && !h.getDate().isBefore(today) && !h.getDate().isAfter(end)) count++;
        }
        return count;
    }

    // ---------------------------------------------------------------- scheduling

    /** Next free slot according to the judge's workload (max 3 hearings per working day). */
    public Proposal propose(User user, int appealId) {
        appealService.getOwned(user, appealId);
        List<Hearing> mine = hearingDao.findByAppeal(appealId);
        LocalDate base = AppClock.today().plusDays(1);
        LocalDate candidate = base;
        int ordinary = 0;
        for (Hearing h : mine) {
            if (h.getDate().plusDays(7).isAfter(candidate)) candidate = h.getDate().plusDays(7);
            if (!"ADDITIONAL".equals(h.getType())) ordinary++;
        }
        List<Hearing> judgeHearings = byJudge(user);
        for (int i = 0; i < 90; i++) {
            LocalDate day = candidate.plusDays(i);
            if (isWeekend(day)) continue;
            int count = 0;
            for (Hearing h : judgeHearings) {
                if (h.getDate().equals(day)) count++;
            }
            if (count >= MAX_PER_DAY) continue;
            for (LocalTime slot : SLOTS) {
                if (!hasConflict(judgeHearings, day, slot)) {
                    return new Proposal(day, slot, ordinary == 0 ? "FIRST" : ordinary == 1 ? "SECOND" : ordinary == 2 ? "THIRD" : "ADDITIONAL");
                }
            }
        }
        return new Proposal(candidate, LocalTime.of(10, 0), "ADDITIONAL");
    }

    public Hearing schedule(User user, int appealId, LocalDate date, LocalTime time, String type) {
        Appeal appeal = appealService.getOwned(user, appealId);
        if (appeal.isConcluded()) {
            throw new BusinessException("No se pueden agendar ponencias en una toca concluida.");
        }
        if (date == null) throw new BusinessException("Seleccione una fecha válida.");
        if (time == null) throw new BusinessException("Seleccione una hora válida.");
        if (type == null || !TYPES.contains(type)) throw new BusinessException("Seleccione el tipo de ponencia.");
        if (date.isBefore(AppClock.today())) throw new BusinessException("La fecha no puede ser anterior a hoy.");
        if (isWeekend(date)) throw new BusinessException("La fecha debe ser un día hábil (de lunes a viernes).");
        if (hasConflict(byJudge(user), date, time)) {
            throw new BusinessException("Ya tiene una ponencia agendada en esa fecha y hora.");
        }

        Hearing h = new Hearing();
        h.setAppealId(appealId);
        h.setJudgeId(user.getId());
        h.setDate(date);
        h.setTime(time);
        h.setType(type);
        h.setStatus("UPCOMING");
        h.setJudgeName(user.getFullName());
        h.setRoomName(user.getRoomName());
        h.setAutoAssigned(false);
        h.setAppeal(appeal);
        hearingDao.add(h);

        AuditEntry entry = new AuditEntry();
        entry.setAppealId(appealId);
        entry.setTimestamp(AppClock.now());
        entry.setUsername(user.getUsername());
        entry.setAction("Agendar ponencia");
        entry.setDetail(FormatUtils.hearingType(type) + " agendada al " + FormatUtils.dateNum(date));
        auditDao.add(entry);

        notificationService.add(user.getId(), appealId, "HEARING_SCHEDULED", "Ponencia agendada",
                "La ponencia de la toca " + appeal.getNumber() + " fue agendada para el " + FormatUtils.dateNum(date) + ".");
        return h;
    }

    private boolean hasConflict(List<Hearing> hearings, LocalDate date, LocalTime time) {
        for (Hearing h : hearings) {
            if (h.getDate().equals(date) && h.getTime().equals(time)) return true;
        }
        return false;
    }

    private boolean isWeekend(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // ---------------------------------------------------------------- agenda

    public List<Hearing> onDate(User user, LocalDate date) {
        List<Hearing> result = new ArrayList<>();
        for (Hearing h : byJudge(user)) {
            if (h.getDate().equals(date)) result.add(h);
        }
        return result;
    }

    /** Whole weeks (Monday first) covering the month. */
    public List<CalendarDay> month(User user, LocalDate firstOfMonth, LocalDate selected) {
        LocalDate today = AppClock.today();
        List<Hearing> all = byJudge(user);
        LocalDate start = firstOfMonth.minusDays(firstOfMonth.getDayOfWeek().getValue() - 1L);
        LocalDate last = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
        LocalDate end = last.plusDays(7L - last.getDayOfWeek().getValue());
        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            CalendarDay cell = new CalendarDay();
            cell.setDate(d);
            cell.setInMonth(d.getMonth() == firstOfMonth.getMonth());
            cell.setToday(d.equals(today));
            cell.setSelected(d.equals(selected));
            boolean upcoming = false;
            boolean done = false;
            int count = 0;
            for (Hearing h : all) {
                if (!h.getDate().equals(d)) continue;
                count++;
                if (h.isUpcoming()) upcoming = true; else done = true;
            }
            cell.setCount(count);
            cell.setState(!cell.isInMonth() ? "" : upcoming ? "UPCOMING" : done ? "DONE" : "");
            days.add(cell);
        }
        return days;
    }

    /** Dates (day numbers) with hearings in a month, e.g. [3, 5, 9]. */
    public List<Integer> daysWithHearings(User user, LocalDate firstOfMonth) {
        List<Integer> days = new ArrayList<>();
        for (Hearing h : byJudge(user)) {
            if (h.getDate().getYear() == firstOfMonth.getYear() && h.getDate().getMonth() == firstOfMonth.getMonth()
                    && !days.contains(h.getDate().getDayOfMonth())) {
                days.add(h.getDate().getDayOfMonth());
            }
        }
        return days;
    }

    public int countInMonth(User user, LocalDate firstOfMonth) {
        int count = 0;
        for (Hearing h : byJudge(user)) {
            if (h.getDate().getYear() == firstOfMonth.getYear() && h.getDate().getMonth() == firstOfMonth.getMonth()) count++;
        }
        return count;
    }

    /** "3, 5 y 9" */
    public static String joinDays(List<Integer> days) {
        if (days.isEmpty()) return "";
        if (days.size() == 1) return String.valueOf(days.get(0));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size() - 1; i++) {
            if (i > 0) sb.append(", ");
            sb.append(days.get(i));
        }
        return sb + " y " + days.get(days.size() - 1);
    }
}
