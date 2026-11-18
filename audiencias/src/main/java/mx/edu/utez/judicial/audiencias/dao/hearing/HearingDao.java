package mx.edu.utez.judicial.audiencias.dao.hearing;

import mx.edu.utez.judicial.audiencias.model.hearing.Hearing;
import mx.edu.utez.judicial.audiencias.model.hearing.HearingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HearingDao {
    Optional<Hearing> findById(int id);
    List<Hearing> findByCaseId(int caseId);
    List<Hearing> findByDeskId(int deskId);
    List<Hearing> findByLawyerId(int lawyerId);
    List<Hearing> findByStatus(HearingStatus status);
    List<Hearing> findScheduledBetween(LocalDateTime from, LocalDateTime to);
    int insert(Hearing hearing);
    void schedule(int id, LocalDateTime hearingDate, String courtroom);
    void changeStatus(int id, HearingStatus status);
    void recordNotes(int id, String notes);
}
