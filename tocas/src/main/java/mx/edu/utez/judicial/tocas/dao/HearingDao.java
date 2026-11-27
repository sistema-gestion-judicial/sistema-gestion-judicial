package mx.edu.utez.judicial.tocas.dao;

import mx.edu.utez.judicial.tocas.model.Hearing;
import mx.edu.utez.judicial.tocas.model.HearingEntry;

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

    public Hearing findById(int id) {
        for (Hearing h : store.hearings) {
            if (h.getId() == id) return h;
        }
        return null;
    }

    public HearingEntry addEntry(Hearing hearing, HearingEntry entry) {
        entry.setId(store.entrySeq.incrementAndGet());
        entry.setHearingId(hearing.getId());
        hearing.getEntries().add(entry);
        return entry;
    }

    public void save(Hearing hearing) {
        int i = store.hearings.indexOf(hearing);
        if (i >= 0) store.hearings.set(i, hearing);
    }

    public Hearing add(Hearing hearing) {
        hearing.setId(store.hearingSeq.incrementAndGet());
        store.hearings.add(hearing);
        return hearing;
    }
}
