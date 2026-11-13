package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Hearing;

import java.util.ArrayList;
import java.util.List;

public class HearingDao {
    private final MemoryStore store = MemoryStore.get();

    public List<Hearing> findByAppeal(int appealId) {
        List<Hearing> result = new ArrayList<>();
        for (Hearing h : store.hearings) {
            if (h.getAppealId() == appealId) result.add(h);
        }
        return result;
    }

    public List<Hearing> findByJudge(int judgeId) {
        List<Hearing> result = new ArrayList<>();
        for (Hearing h : store.hearings) {
            if (h.getJudgeId() == judgeId) result.add(h);
        }
        return result;
    }

    public Hearing add(Hearing hearing) {
        hearing.setId(store.hearingSeq.incrementAndGet());
        store.hearings.add(hearing);
        return hearing;
    }
}
