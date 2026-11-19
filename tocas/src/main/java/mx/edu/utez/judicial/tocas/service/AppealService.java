package mx.edu.utez.judicial.tocas.service;

import mx.edu.utez.judicial.tocas.dao.*;
import mx.edu.utez.judicial.tocas.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Appeals ("tocas") of a judge: list, detail, timelines and conclusion. */
public class AppealService {
    public static final int PAGE_SIZE = 4;

    private final AppealDao appealDao = new AppealDao();
    private final HearingDao hearingDao = new HearingDao();
    private final ResolutionDao resolutionDao = new ResolutionDao();
    private final AuditDao auditDao = new AuditDao();
    private final DocumentDao documentDao = new DocumentDao();

    public int countAssigned(User user) {
        return appealDao.findByOwner(user.getId()).size();
    }

    /** Returns the appeal only when it belongs to the user. */
    public Appeal getOwned(User user, int id) {
        Appeal appeal = appealDao.findById(id);
        if (appeal == null || user == null || appeal.getOwnerId() != user.getId()) {
            throw new NotFoundException("Toca no encontrada.");
        }
        return appeal;
    }

    public PageResult<Appeal> search(User user, String query, String status, LocalDate from, LocalDate to, int page) {
        LocalDate today = AppClock.today();
        String needle = query == null ? "" : query.trim().toLowerCase();
        List<Appeal> filtered = new ArrayList<>();
        for (Appeal a : appealDao.findByOwner(user.getId())) {
            if (!status.isEmpty() && !status.equals(a.getStatus())) continue;
            if (from != null && a.getReceivedDate().isBefore(from)) continue;
            if (to != null && a.getReceivedDate().isAfter(to)) continue;
            String haystack = (a.getNumber() + " " + a.getOriginCourt() + " " + a.getPlaintiff() + " " + a.getDefendant()).toLowerCase();
            if (!needle.isEmpty() && !haystack.contains(needle)) continue;
            a.setNextHearing(nextHearing(a.getId(), today));
            filtered.add(a);
        }
        filtered.sort((x, y) -> {
            Hearing hx = x.getNextHearing();
            Hearing hy = y.getNextHearing();
            if (hx != null && hy != null) {
                int c = hx.getDate().compareTo(hy.getDate());
                return c != 0 ? c : hx.getTime().compareTo(hy.getTime());
            }
            if (hx != null) return -1;
            if (hy != null) return 1;
            return y.getReceivedDate().compareTo(x.getReceivedDate());
        });
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int current = Math.min(Math.max(page, 1), pages);
        int fromIndex = (current - 1) * PAGE_SIZE;
        List<Appeal> items = filtered.subList(fromIndex, Math.min(total, fromIndex + PAGE_SIZE));
        return new PageResult<>(new ArrayList<>(items), total, current, PAGE_SIZE);
    }

    private Hearing nextHearing(int appealId, LocalDate today) {
        Hearing best = null;
        for (Hearing h : hearingDao.findByAppeal(appealId)) {
            if (!"UPCOMING".equals(h.getStatus()) || h.getDate().isBefore(today)) continue;
            if (best == null || h.getDate().isBefore(best.getDate())) best = h;
        }
        return best;
    }

    public List<CaseDocument> documents(Appeal appeal) {
        List<CaseDocument> docs = documentDao.findByAppeal(appeal);
        docs.sort(Comparator.comparing(CaseDocument::getDate));
        return docs;
    }

    public List<AuditEntry> audit(Appeal appeal) {
        return auditDao.findByAppeal(appeal.getId());
    }

    /** Concludes the appeal. Requires a finalized resolution and the signed PDF. */
    public void conclude(User user, int appealId) {
        Appeal appeal = getOwned(user, appealId);
        if (appeal.isConcluded()) {
            throw new BusinessException("La toca ya fue concluida.");
        }
        Resolution r = resolutionDao.findByAppeal(appealId);
        if (r == null || !r.isFinalized()) {
            throw new BusinessException("Debe finalizar la resolución antes de concluir la toca.");
        }
        if (!r.isSigned()) {
            throw new BusinessException("Debe cargar el PDF firmado antes de concluir la toca.");
        }
        appeal.setStatus("CONCLUDED");
        appeal.setConcludedDate(AppClock.today());
        appealDao.update(appeal);
        AuditEntry entry = new AuditEntry();
        entry.setAppealId(appealId);
        entry.setTimestamp(AppClock.now());
        entry.setUsername(user.getUsername());
        entry.setAction("Concluir toca");
        entry.setDetail("Toca concluida");
        auditDao.add(entry);
    }

    // ---------------------------------------------------------------- timelines

    /** Timeline shown in the "Resumen" tab. */
    public List<TimelineStep> summaryTimeline(Appeal appeal, List<Hearing> hearings, Resolution resolution) {
        LocalDate today = AppClock.today();
        List<TimelineStep> steps = new ArrayList<>();
        steps.add(step("Toca recibida", FormatUtils.dateNum(appeal.getReceivedDate()), true, null));

        LocalDate routed = appeal.getReceivedDate();
        for (AuditEntry e : auditDao.findByAppeal(appeal.getId())) {
            if ("Turnar a ponencia".equals(e.getAction())) routed = e.getTimestamp().toLocalDate();
        }
        steps.add(step("Turnada a ponencia", FormatUtils.dateNum(routed), true, null));

        Hearing upcoming = null;
        Hearing lastDone = null;
        for (Hearing h : hearings) {
            if ("UPCOMING".equals(h.getStatus()) && !h.getDate().isBefore(today)) {
                if (upcoming == null || h.getDate().isBefore(upcoming.getDate())) upcoming = h;
            } else if (lastDone == null || h.getDate().isAfter(lastDone.getDate())) {
                lastDone = h;
            }
        }
        if (upcoming != null) {
            steps.add(step(FormatUtils.hearingType(upcoming.getType()), FormatUtils.dateNum(upcoming.getDate()), false, "Próxima"));
        } else if (lastDone != null) {
            steps.add(step(FormatUtils.hearingType(lastDone.getType()), FormatUtils.dateNum(lastDone.getDate()), true, null));
        } else {
            steps.add(step("Ponencia", "Pendiente", false, null));
        }

        boolean resolved = resolution != null && resolution.isFinalized();
        steps.add(step("Resolución", resolved ? FormatUtils.dateNum(resolution.getFinalizedAt().toLocalDate()) : "Pendiente", resolved, null));
        return markCurrent(steps);
    }

    /** Timeline shown in the "Resolución" tab. */
    public List<TimelineStep> resolutionTimeline(Appeal appeal, Resolution r) {
        List<TimelineStep> steps = new ArrayList<>();
        steps.add(step("Redacción iniciada", r == null ? "Pendiente" : FormatUtils.dateNum(r.getStartedDate()), r != null, null));
        if (r != null && r.isFinalized()) {
            steps.add(step("Resolución finalizada", FormatUtils.dateNum(r.getFinalizedAt().toLocalDate()), true, null));
        } else {
            steps.add(step("Borrador guardado", r == null ? "Pendiente" : FormatUtils.dateNum(r.getLastModified().toLocalDate()), false, null));
        }
        boolean signed = r != null && r.isSigned();
        steps.add(step("Firmada / escaneada", signed ? FormatUtils.dateNum(r.getSignedUploadedAt().toLocalDate()) : "Pendiente", signed, null));
        steps.add(step("Concluida", appeal.isConcluded() ? FormatUtils.dateNum(appeal.getConcludedDate()) : "Pendiente", appeal.isConcluded(), null));
        return markCurrent(steps);
    }

    private TimelineStep step(String label, String dateText, boolean done, String badge) {
        TimelineStep s = new TimelineStep();
        s.setLabel(label);
        s.setDateText(dateText);
        s.setState(done ? "DONE" : "PENDING");
        s.setBadge(badge);
        return s;
    }

    /** The first step that is not done is the current one. */
    private List<TimelineStep> markCurrent(List<TimelineStep> steps) {
        for (TimelineStep s : steps) {
            if ("PENDING".equals(s.getState())) {
                s.setState("CURRENT");
                break;
            }
        }
        return steps;
    }
}
