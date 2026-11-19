package mx.edu.utez.judicial.tocas.service;

import mx.edu.utez.judicial.tocas.dao.AppealDao;
import mx.edu.utez.judicial.tocas.dao.AuditDao;
import mx.edu.utez.judicial.tocas.dao.ResolutionDao;
import mx.edu.utez.judicial.tocas.model.Appeal;
import mx.edu.utez.judicial.tocas.model.AuditEntry;
import mx.edu.utez.judicial.tocas.model.Resolution;
import mx.edu.utez.judicial.tocas.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolution life cycle: create draft, edit, finalize, upload the signed PDF. */
public class ResolutionService {
    public static final long MAX_UPLOAD_BYTES = 20L * 1024 * 1024;

    private static final Set<String> ALLOWED_TAGS = Set.of("b", "i", "u", "strong", "em", "ul", "ol", "li", "br", "p", "div", "span");
    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z0-9]+)([^>]*)>");
    private static final Pattern ALIGN = Pattern.compile("text-align\\s*:\\s*(left|right|center|justify)");
    private static final Pattern PDF_PAGE = Pattern.compile("/Type\\s*/Page(?![s])");

    private final ResolutionDao resolutionDao = new ResolutionDao();
    private final AppealDao appealDao = new AppealDao();
    private final AuditDao auditDao = new AuditDao();
    private final AppealService appealService = new AppealService();

    public Resolution find(User user, int appealId) {
        appealService.getOwned(user, appealId);
        return resolutionDao.findByAppeal(appealId);
    }

    public int countDrafts(User user) {
        int count = 0;
        for (Appeal a : appealDao.findByOwner(user.getId())) {
            Resolution r = resolutionDao.findByAppeal(a.getId());
            if (r != null && r.isDraft() && !a.isConcluded()) count++;
        }
        return count;
    }

    public Resolution create(User user, int appealId) {
        Appeal appeal = appealService.getOwned(user, appealId);
        if (appeal.isConcluded()) throw new BusinessException("La toca ya fue concluida.");
        Resolution existing = resolutionDao.findByAppeal(appealId);
        if (existing != null) return existing;
        Resolution r = new Resolution();
        r.setAppealId(appealId);
        r.setStatus("DRAFT");
        r.setVersion(1);
        r.setStartedDate(AppClock.today());
        r.setLastModified(AppClock.now());
        r.setResultando("");
        r.setConsiderando("");
        r.setResuelve("");
        r.setPages(1);
        resolutionDao.save(r);
        log(user, appealId, "Crear resolución", "Inició la redacción de la resolución");
        return r;
    }

    /** Saves the draft. An autosave keeps the same version and writes no audit entry. */
    public Resolution saveDraft(User user, int appealId, String resultando, String considerando, String resuelve, boolean autosave) {
        Resolution r = requireDraft(user, appealId);
        applyContent(r, resultando, considerando, resuelve);
        if (!autosave) {
            r.setVersion(r.getVersion() + 1);
            log(user, appealId, "Editar resolución", "Guardó la versión " + r.getVersion() + " del borrador");
        }
        resolutionDao.save(r);
        return r;
    }

    public Resolution finalizeResolution(User user, int appealId, String resultando, String considerando, String resuelve) {
        Resolution r = requireDraft(user, appealId);
        applyContent(r, resultando, considerando, resuelve);
        if (textLength(r.getResultando()) == 0 || textLength(r.getConsiderando()) == 0 || textLength(r.getResuelve()) == 0) {
            resolutionDao.save(r);
            throw new BusinessException("Complete Resultando, Considerando y Resuelve antes de finalizar. El borrador se guardó.");
        }
        r.setStatus("FINALIZED");
        r.setFinalizedAt(AppClock.now());
        resolutionDao.save(r);
        log(user, appealId, "Finalizar resolución", "Finalizó la resolución (versión " + r.getVersion() + ")");
        return r;
    }

    public void uploadSigned(User user, int appealId, String fileName, InputStream in) throws IOException {
        Appeal appeal = appealService.getOwned(user, appealId);
        Resolution r = resolutionDao.findByAppeal(appealId);
        if (appeal.isConcluded()) throw new BusinessException("La toca ya fue concluida.");
        if (r == null || !r.isFinalized()) throw new BusinessException("Primero debe finalizar la resolución.");
        if (r.isSigned()) throw new BusinessException("Ya se cargó la resolución firmada.");
        String clean = fileName == null ? "" : fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
        if (!clean.toLowerCase().endsWith(".pdf")) throw new BusinessException("Solo se permiten archivos PDF.");
        byte[] data = in.readNBytes((int) MAX_UPLOAD_BYTES + 1);
        if (data.length > MAX_UPLOAD_BYTES) throw new BusinessException("El archivo excede el máximo de 20 MB.");
        if (data.length < 5 || !new String(data, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF")) {
            throw new BusinessException("El archivo no es un PDF válido.");
        }
        Path dir = AppConfig.uploadDir().resolve(String.valueOf(appealId));
        Files.createDirectories(dir);
        Path file = dir.resolve(UUID.randomUUID() + ".pdf");
        Files.write(file, data);

        Matcher m = PDF_PAGE.matcher(new String(data, StandardCharsets.ISO_8859_1));
        int pages = 0;
        while (m.find()) pages++;
        r.setSignedFileName(clean);
        r.setSignedPages(Math.max(1, pages));
        r.setSignedSize(data.length);
        r.setSignedUploadedAt(AppClock.now());
        r.setSignedPath(file.toString());
        resolutionDao.save(r);
        log(user, appealId, "Subir resolución firmada", "Cargó el PDF firmado " + clean);
    }

    /** Bytes of the signed PDF (a tiny placeholder PDF for the sample data that has no real file). */
    public byte[] signedBytes(User user, int appealId) throws IOException {
        Appeal appeal = appealService.getOwned(user, appealId);
        Resolution r = resolutionDao.findByAppeal(appealId);
        if (r == null || !r.isSigned()) throw new NotFoundException("No hay resolución firmada.");
        if (r.getSignedPath() != null && Files.exists(Path.of(r.getSignedPath()))) {
            return Files.readAllBytes(Path.of(r.getSignedPath()));
        }
        return placeholderPdf("Resolucion firmada - " + appeal.getNumber());
    }

    // ---------------------------------------------------------------- helpers

    private Resolution requireDraft(User user, int appealId) {
        Appeal appeal = appealService.getOwned(user, appealId);
        Resolution r = resolutionDao.findByAppeal(appealId);
        if (appeal.isConcluded() || r == null || !r.isDraft()) {
            throw new BusinessException("La resolución no se puede editar.");
        }
        return r;
    }

    private void applyContent(Resolution r, String resultando, String considerando, String resuelve) {
        r.setResultando(sanitize(resultando));
        r.setConsiderando(sanitize(considerando));
        r.setResuelve(sanitize(resuelve));
        r.setLastModified(AppClock.now());
        int chars = textLength(r.getResultando()) + textLength(r.getConsiderando()) + textLength(r.getResuelve());
        r.setPages(Math.max(1, (int) Math.ceil(chars / 2200.0)));
    }

    private void log(User user, int appealId, String action, String detail) {
        AuditEntry e = new AuditEntry();
        e.setAppealId(appealId);
        e.setTimestamp(AppClock.now());
        e.setUsername(user.getUsername());
        e.setAction(action);
        e.setDetail(detail);
        auditDao.add(e);
    }

    static int textLength(String html) {
        if (html == null) return 0;
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim().length();
    }

    /** Keeps only simple formatting tags (no scripts, no attributes except text alignment). */
    static String sanitize(String html) {
        if (html == null) return "";
        String s = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", "");
        Matcher m = TAG.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String tag = m.group(2).toLowerCase();
            String replacement = "";
            if (ALLOWED_TAGS.contains(tag)) {
                if (!m.group(1).isEmpty()) {
                    replacement = "</" + tag + ">";
                } else {
                    replacement = "<" + tag;
                    Matcher a = ALIGN.matcher(m.group(3));
                    if (a.find()) replacement += " style=\"text-align:" + a.group(1) + "\"";
                    replacement += ">";
                }
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString().trim();
    }

    private static byte[] placeholderPdf(String text) {
        String safe = text.replaceAll("[^A-Za-z0-9 .\\-/]", "");
        String stream = "BT /F1 18 Tf 72 720 Td (" + safe + ") Tj ET";
        String[] objs = {
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
            "<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        };
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objs.length];
        for (int i = 0; i < objs.length; i++) {
            offsets[i] = pdf.length();
            pdf.append(i + 1).append(" 0 obj\n").append(objs[i]).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objs.length + 1).append("\n0000000000 65535 f \n");
        for (int off : offsets) pdf.append(String.format("%010d 00000 n \n", off));
        pdf.append("trailer\n<< /Size ").append(objs.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}
