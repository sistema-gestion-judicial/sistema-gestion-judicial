package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Appeal;
import mx.edu.utez.judicial.tocas.model.CaseDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Case file documents ("actuaciones"). Generated from a template until a real file store exists. */
public class DocumentDao {

    public List<CaseDocument> findByAppeal(Appeal appeal) {
        List<CaseDocument> docs = new ArrayList<>();
        int id = appeal.getId() * 10;
        String court = appeal.getOriginCourt();
        docs.add(doc(id + 1, appeal, "Demanda inicial", 12, LocalDate.of(2025, 3, 14), "FIRST", court));
        docs.add(doc(id + 2, appeal, "Contestación de la demanda", 8, LocalDate.of(2025, 4, 2), "FIRST", court));
        docs.add(doc(id + 3, appeal, "Acta de audiencia de pruebas", 6, LocalDate.of(2026, 2, 11), "FIRST", court));
        docs.add(doc(id + 4, appeal, "Dictamen pericial", 21, LocalDate.of(2026, 5, 20), "FIRST", court));
        docs.add(doc(id + 5, appeal, "Sentencia recurrida", 14, LocalDate.of(2026, 8, 28), "FIRST", court));
        LocalDate received = appeal.getReceivedDate();
        docs.add(doc(id + 6, appeal, "Expresión de agravios", 9, received.minusDays(10), "APPEAL", appeal.getRoomName()));
        docs.add(doc(id + 7, appeal, "Auto de admisión de la apelación", 2, received.plusDays(3), "APPEAL", appeal.getRoomName()));
        return docs;
    }

    private CaseDocument doc(int id, Appeal appeal, String name, int pages, LocalDate date, String instance, String court) {
        CaseDocument d = new CaseDocument();
        d.setId(id);
        d.setAppealId(appeal.getId());
        d.setName(name);
        d.setPages(pages);
        d.setDate(date);
        d.setInstance(instance);
        d.setCourt(court);
        return d;
    }
}
