package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Same catalogs as V001__create_catalogs.sql, for running without a database.
 */
public class InMemoryCatalogDao implements CatalogDao {

    private static final List<Court> COURTS = List.of(
        new Court("01", "Juzgado de Cuernavaca", "Distrito judicial de Cuernavaca", true),
        new Court("05", "Juzgado de Yautepec", "Distrito judicial de Yautepec", true),
        new Court("07", "Juzgado de Jojutla", "Distrito judicial de Jojutla", true),
        new Court("12", "Juzgado de Cuautla", "Distrito judicial de Cuautla", true));

    private static final List<SubjectMatter> SUBJECT_MATTERS = List.of(
        new SubjectMatter(1, "Civil", true), new SubjectMatter(2, "Familiar", true),
        new SubjectMatter(3, "Mercantil", true), new SubjectMatter(4, "Penal", true));

    @Override
    public List<Court> findCourts() {
        return COURTS;
    }

    @Override
    public Optional<Court> findCourt(String code) {
        return COURTS.stream().filter(court -> court.getCode().equals(code)).findFirst();
    }

    @Override
    public List<SubjectMatter> findSubjectMatters() {
        return SUBJECT_MATTERS;
    }
}
