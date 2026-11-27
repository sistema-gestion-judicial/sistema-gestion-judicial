package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.*;
import mx.edu.utez.judicial.tocas.service.PasswordHasher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Temporary in-memory data (the database is not ready yet).
 * When the database exists, rewrite the DAO methods with JDBC (see sql/schema.sql and DbConnection)
 * and delete this class.
 */
final class MemoryStore {
    private static final MemoryStore INSTANCE = new MemoryStore();

    static MemoryStore get() {
        return INSTANCE;
    }

    final Map<Integer, User> users = new ConcurrentHashMap<>();
    final Map<Integer, Appeal> appeals = new ConcurrentHashMap<>();
    final List<Hearing> hearings = new CopyOnWriteArrayList<>();
    final List<Notification> notifications = new CopyOnWriteArrayList<>();
    final List<AuditEntry> audit = new CopyOnWriteArrayList<>();
    final Map<Integer, Resolution> resolutions = new ConcurrentHashMap<>();
    final AtomicInteger hearingSeq = new AtomicInteger(1000);
    final AtomicInteger notificationSeq = new AtomicInteger(1000);
    final AtomicInteger auditSeq = new AtomicInteger(1000);
    final AtomicInteger entrySeq = new AtomicInteger(1000);

    private MemoryStore() {
        seed();
    }

    // ---------------------------------------------------------------- seed data

    private void seed() {
        User judge = user(1, "rsalgado", "Ricardo Salgado", "rsalgado@utez.edu.mx", "Magistrado", "Segunda Sala Civil", "Magistrado2026!");
        User judge2 = user(2, "mperez", "María Pérez", "mperez@utez.edu.mx", "Magistrada", "Primera Sala Civil", "Perez2026!");

        // The five appeals that appear in the Figma designs
        appeal(418, "Paloma", "Alejandro", "Juzgado de Zapata", LocalDate.of(2026, 10, 2), "0412/2025", judge);
        appeal(391, "Canelo Álvarez", "Gilberto Mora", "Juzgado 2.º Civil", LocalDate.of(2026, 9, 28), null, judge);
        appeal(402, "Lian Joel", "Ángel Rafael", "Juzgado de Cuautla", LocalDate.of(2026, 10, 5), null, judge);
        appeal(377, "Alonso Caleb", "Alexis Arriola", "Juzgado 3.º Civil", LocalDate.of(2026, 9, 21), null, judge);
        appeal(365, "Diego Sánchez", "Víctor Flores", "Juzgado de Jojutla", LocalDate.of(2026, 9, 30), null, judge);

        hearing(418, LocalDate.of(2026, 10, 5), LocalTime.of(9, 0), "ADDITIONAL", "DONE", judge, false);
        hearing(418, LocalDate.of(2026, 10, 26), LocalTime.of(10, 0), "FIRST", "UPCOMING", judge, true);
        hearing(418, LocalDate.of(2026, 11, 9), LocalTime.of(11, 30), "SECOND", "UPCOMING", judge, false);
        hearing(391, LocalDate.of(2026, 10, 27), LocalTime.of(9, 30), "FIRST", "UPCOMING", judge, true);
        hearing(402, LocalDate.of(2026, 10, 29), LocalTime.of(11, 0), "FIRST", "UPCOMING", judge, true);
        hearing(377, LocalDate.of(2026, 11, 3), LocalTime.of(10, 30), "FIRST", "UPCOMING", judge, true);
        hearing(365, LocalDate.of(2026, 11, 5), LocalTime.of(12, 0), "FIRST", "UPCOMING", judge, true);

        // Audit trail of appeal 418 (as in the Bitácora screen)
        audit(418, LocalDateTime.of(2026, 10, 20, 16, 35), "rsalgado", "Editar resolución", "Guardó la versión 3 del borrador");
        audit(418, LocalDateTime.of(2026, 10, 15, 13, 12), "rsalgado", "Editar resolución", "Guardó la versión 2 del borrador");
        audit(418, LocalDateTime.of(2026, 10, 8, 10, 3), "rsalgado", "Crear resolución", "Inició la redacción de la resolución");
        audit(418, LocalDateTime.of(2026, 10, 5, 9, 40), "Sistema", "Agendar ponencia", "1.ª ponencia asignada al 26/10/2026");
        audit(418, LocalDateTime.of(2026, 10, 5, 9, 24), "lmendez", "Turnar a ponencia", "Turnada a Ricardo Salgado");
        audit(418, LocalDateTime.of(2026, 10, 2, 9, 24), "Oficialía de Partes", "Recibir toca", "Recibida en Segunda Sala Civil");

        Resolution draft = new Resolution();
        draft.setAppealId(418);
        draft.setStatus("DRAFT");
        draft.setVersion(3);
        draft.setStartedDate(LocalDate.of(2026, 10, 8));
        draft.setLastModified(LocalDateTime.of(2026, 10, 20, 16, 35));
        draft.setResultando("<p>1. Con fecha 02 de octubre de 2026 se recibió en esta Sala el recurso de apelación interpuesto por la parte demandada en contra de la sentencia dictada el 28 de agosto de 2026.</p><p>2. Mediante auto del 05 de octubre de 2026 se admitió el recurso y se turnó el expediente a esta ponencia.</p>");
        draft.setConsiderando("<p>I. Esta Sala es competente para conocer del presente recurso de apelación.</p><p>II. Los agravios se analizan en su conjunto.</p>");
        draft.setResuelve("");
        draft.setPages(12);
        resolutions.put(418, draft);

        // Notifications of judge 1 (as in the Notificaciones screen)
        notification(1, 391, "HEARING_SCHEDULED", "Ponencia agendada", "La ponencia de la toca T-000391/2026 fue agendada para el 27/10/2026.", LocalDateTime.of(2026, 10, 24, 8, 30), false);
        notification(1, 418, "HEARING_NEAR", "Ponencia próxima", "La ponencia de la toca T-000418/2026 es en 3 días (26/10/2026).", LocalDateTime.of(2026, 10, 23, 12, 10), false);
        notification(1, 418, "NEW_APPEAL", "Nueva toca asignada a su sala", "Toca T-000418/2026 · Paloma vs Alejandro · Juzgado de Zapata", LocalDateTime.of(2026, 10, 5, 9, 24), true);
        notification(1, 402, "FILE_RECEIVED", "Expediente recibido de Oficialía de Partes", "Toca T-000402/2026 · Lian Joel vs Ángel Rafael · Juzgado de Cuautla", LocalDateTime.of(2026, 10, 5, 9, 15), true);
        notification(1, 365, "NEW_APPEAL", "Nueva toca asignada a su sala", "Toca T-000365/2026 · Diego Sánchez vs Víctor Flores · Juzgado de Jojutla", LocalDateTime.of(2026, 9, 30, 16, 20), true);
        notification(1, 391, "NEW_APPEAL", "Nueva toca asignada a su sala", "Toca T-000391/2026 · Canelo Álvarez vs Gilberto Mora · Juzgado 2.º Civil", LocalDateTime.of(2026, 9, 28, 10, 5), true);

        // Filler appeals so the list has 36 records (9 pages of 4)
        String[] plaintiffs = {"Marco Ruiz", "Elena Castillo", "Jorge Návar", "Sofía Landa", "Iván Duarte", "Carmen Ortiz", "Raúl Benítez", "Lucía Mejía"};
        String[] defendants = {"Hugo Barrera", "Patricia Solís", "Andrés Quiroz", "Valeria Núñez", "Tomás Cano", "Beatriz Rangel", "Felipe Armenta", "Daniela Orozco"};
        String[] courts = {"Juzgado 1.º Civil", "Juzgado de Cuernavaca", "Juzgado de Jiutepec", "Juzgado de Temixco", "Juzgado de Yautepec", "Juzgado 4.º Civil"};
        for (int i = 0; i < 31; i++) {
            int id = 300 + i;
            Appeal a = appeal(id, plaintiffs[i % 8], defendants[(i * 3 + 1) % 8], courts[i % 6], LocalDate.of(2026, 9, 25).minusDays(i), null, judge);
            if (i % 3 == 0) {
                a.setStatus("CONCLUDED");
                a.setConcludedDate(a.getReceivedDate().plusDays(20));
                Resolution r = new Resolution();
                r.setAppealId(id);
                r.setStatus("FINALIZED");
                r.setVersion(2);
                r.setStartedDate(a.getReceivedDate().plusDays(8));
                r.setLastModified(a.getReceivedDate().plusDays(15).atTime(10, 20));
                r.setFinalizedAt(a.getReceivedDate().plusDays(15).atTime(10, 20));
                r.setResultando("<p>Resultando de la toca " + a.getNumber() + ".</p>");
                r.setConsiderando("<p>Considerando de la toca " + a.getNumber() + ".</p>");
                r.setResuelve("<p>Se confirma la sentencia recurrida.</p>");
                r.setPages(8);
                r.setSignedFileName("Resolucion_" + a.getNumber().replace('/', '-') + "_firmada.pdf");
                r.setSignedPages(3);
                r.setSignedSize(2_400_000L);
                r.setSignedUploadedAt(a.getReceivedDate().plusDays(18).atTime(11, 0));
                resolutions.put(id, r);
            }
        }
        hearing(300, LocalDate.of(2026, 10, 8), LocalTime.of(10, 0), "FIRST", "DONE", judge, true);
        hearing(303, LocalDate.of(2026, 10, 14), LocalTime.of(10, 0), "FIRST", "DONE", judge, true);

        // A second judge, so the app can be tried with another account
        for (int i = 0; i < 6; i++) {
            appeal(500 + i, plaintiffs[(i + 2) % 8], defendants[(i + 5) % 8], courts[i % 6], LocalDate.of(2026, 10, 1).plusDays(i), null, judge2);
        }
        hearing(500, LocalDate.of(2026, 10, 28), LocalTime.of(10, 0), "FIRST", "UPCOMING", judge2, true);
        hearing(501, LocalDate.of(2026, 11, 4), LocalTime.of(12, 0), "FIRST", "UPCOMING", judge2, true);
        notification(2, 500, "NEW_APPEAL", "Nueva toca asignada a su sala", "Toca T-000500/2026 · Elena Castillo vs Valeria Núñez · Juzgado 1.º Civil", LocalDateTime.of(2026, 10, 1, 9, 0), true);

        // Every appeal gets hearings, audit trail and an initial resolution so any of them works end to end
        int k = 0;
        java.util.List<Appeal> ordered = new java.util.ArrayList<>(appeals.values());
        ordered.sort(java.util.Comparator.comparingInt(Appeal::getId));
        for (Appeal a : ordered) {
            if (a.getId() == 418) continue;
            completeDemo(a, a.getOwnerId() == 1 ? judge : judge2, k++);
        }
        seedHearingEntries();
    }

    /** Record of the hearing in progress (T-000418, 26/10) as in Figma, plus a short record for every finished hearing. */
    private void seedHearingEntries() {
        String[][] script = {
            {"ATTENDANCE", "Se da inicio a la ponencia, comparecen ambas partes."},
            {"EXPOSITION", "La parte apelante expone sus agravios."},
            {"EVIDENCE", "Se admiten pruebas documentales."},
            {"AGREEMENT", "Se cierra el periodo de alegatos."}
        };
        int[][] offsets = {{2, 14}, {5, 40}, {20, 3}, {41, 27}};
        for (Hearing h : hearings) {
            boolean live = h.getAppealId() == 418 && h.getDate().equals(LocalDate.of(2026, 10, 26));
            if (!live && !"DONE".equals(h.getStatus())) continue;
            LocalDateTime base = LocalDateTime.of(h.getDate(), h.getTime());
            int n = live ? 4 : 3;
            LocalDateTime last = base;
            for (int i = 0; i < n; i++) {
                HearingEntry e = new HearingEntry();
                e.setId(entrySeq.incrementAndGet());
                e.setHearingId(h.getId());
                last = base.plusMinutes(offsets[i][0]).plusSeconds(offsets[i][1]);
                e.setTimestamp(last);
                e.setType(script[i][0]);
                e.setText(script[i][1]);
                e.setEdited(live && i == 2);
                h.getEntries().add(e);
                if (i == 0) h.setStartedAt(last);
            }
            if (!live) h.setFinishedAt(last.plusMinutes(1));
        }
    }

    /** Fills missing demo data of an appeal: hearing, audit trail and initial resolution draft. */
    private void completeDemo(Appeal a, User judge, int k) {
        int id = a.getId();
        boolean hasHearing = false;
        for (Hearing h : hearings) if (h.getAppealId() == id) { hasHearing = true; break; }
        LocalDate received = a.getReceivedDate();
        if (!hasHearing) {
            if (a.isConcluded()) {
                hearing(id, nextWorkday(received.plusDays(10)), LocalTime.of(10, 0), "FIRST", "DONE", judge, true);
            } else {
                hearing(id, nextWorkday(LocalDate.of(2026, 11, 10).plusDays(k)), k % 2 == 0 ? LocalTime.of(10, 0) : LocalTime.of(12, 30), "FIRST", "UPCOMING", judge, true);
            }
        }
        boolean hasRouted = false;
        for (AuditEntry e : audit) if (e.getAppealId() == id && "Turnar a ponencia".equals(e.getAction())) hasRouted = true;
        if (!hasRouted) {
            audit(id, received.atTime(10, 5), "lmendez", "Turnar a ponencia", "Turnada a " + judge.getFullName());
            audit(id, received.atTime(10, 20), "Sistema", "Agendar ponencia", "1.ª ponencia asignada automáticamente");
        }
        Resolution existing = resolutions.get(id);
        if (existing == null) {
            Resolution r = new Resolution();
            r.setAppealId(id);
            r.setStatus("DRAFT");
            r.setVersion(1);
            r.setStartedDate(received.plusDays(3));
            r.setLastModified(received.plusDays(3).atTime(12, 0));
            r.setResultando("<p>1. Con fecha " + FormatDate.num(received) + " se recibió en esta Sala el recurso de apelación interpuesto por " + a.getPlaintiff() + " en contra de la sentencia dictada por el " + a.getOriginCourt() + ".</p>");
            r.setConsiderando("<p>I. Esta Sala es competente para conocer del presente recurso de apelación.</p>");
            r.setResuelve("");
            r.setPages(1);
            resolutions.put(id, r);
            audit(id, received.plusDays(3).atTime(12, 0), judge.getUsername(), "Crear resolución", "Inició la redacción de la resolución");
        } else if (existing.isFinalized()) {
            audit(id, existing.getStartedDate().atTime(9, 30), judge.getUsername(), "Crear resolución", "Inició la redacción de la resolución");
            audit(id, existing.getFinalizedAt(), judge.getUsername(), "Finalizar resolución", "Finalizó la resolución (versión " + existing.getVersion() + ")");
            if (existing.isSigned()) audit(id, existing.getSignedUploadedAt(), judge.getUsername(), "Subir resolución firmada", "Cargó el PDF firmado " + existing.getSignedFileName());
            if (a.getConcludedDate() != null) audit(id, a.getConcludedDate().atTime(13, 0), judge.getUsername(), "Concluir toca", "Toca concluida");
        }
    }

    private static LocalDate nextWorkday(LocalDate d) {
        while (d.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) d = d.plusDays(1);
        return d;
    }

    private static final class FormatDate {
        static String num(LocalDate d) { return mx.edu.utez.judicial.tocas.service.FormatUtils.dateNum(d); }
    }

    private User user(int id, String username, String fullName, String email, String title, String room, String password) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setRoleTitle(title);
        u.setRoomName(room);
        u.setSalt(PasswordHasher.newSalt());
        u.setPasswordHash(PasswordHasher.hash(password, u.getSalt()));
        users.put(id, u);
        return u;
    }

    private Appeal appeal(int id, String plaintiff, String defendant, String court, LocalDate received, String originFile, User owner) {
        Appeal a = new Appeal();
        a.setId(id);
        a.setNumber(String.format("T-%06d/2026", id));
        a.setPlaintiff(plaintiff);
        a.setDefendant(defendant);
        a.setOriginCourt(court);
        a.setReceivedDate(received);
        a.setAppealType("Apelación");
        a.setOriginFile(originFile != null ? originFile : String.format("%04d/2025", id));
        a.setRoomName(owner.getRoomName());
        a.setStatus("NEW");
        a.setOwnerId(owner.getId());
        appeals.put(id, a);
        if (id != 418) {
            audit(id, received.atTime(9, 24), "Oficialía de Partes", "Recibir toca", "Recibida en " + owner.getRoomName());
        }
        return a;
    }

    private void hearing(int appealId, LocalDate date, LocalTime time, String type, String status, User judge, boolean auto) {
        Hearing h = new Hearing();
        h.setId(hearingSeq.incrementAndGet());
        h.setAppealId(appealId);
        h.setJudgeId(judge.getId());
        h.setDate(date);
        h.setTime(time);
        h.setType(type);
        h.setStatus(status);
        h.setJudgeName(judge.getFullName());
        h.setRoomName(judge.getRoomName());
        h.setAutoAssigned(auto);
        h.setAppeal(appeals.get(appealId));
        hearings.add(h);
    }

    private void audit(int appealId, LocalDateTime when, String username, String action, String detail) {
        AuditEntry e = new AuditEntry();
        e.setId(auditSeq.incrementAndGet());
        e.setAppealId(appealId);
        e.setTimestamp(when);
        e.setUsername(username);
        e.setAction(action);
        e.setDetail(detail);
        audit.add(e);
    }

    private void notification(int userId, int appealId, String type, String title, String message, LocalDateTime when, boolean read) {
        Notification n = new Notification();
        n.setId(notificationSeq.incrementAndGet());
        n.setUserId(userId);
        n.setAppealId(appealId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setCreatedAt(when);
        n.setRead(read);
        notifications.add(n);
    }
}
