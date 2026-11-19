package mx.edu.utez.judicial.audiencias.dao;

import mx.edu.utez.judicial.audiencias.model.CaseDocument;

import java.util.List;
import java.util.Optional;

public interface CaseDocumetDao {
    public Optional<CaseDocument> findById(int id);
    public List<CaseDocument> findByCaseId(int caseId);
    public int insert(CaseDocument document);
    public void delete(int id);

}
