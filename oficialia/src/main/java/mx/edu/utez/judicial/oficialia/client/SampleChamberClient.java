package mx.edu.utez.judicial.oficialia.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.Chamber;

/**
 * The chambers of the Tribunal Superior de Justicia until the Tocas team publishes its API in contracts/ (#124).
 * They are ordered by a rough distance from the court's seat. Start Tomcat with -Doficialia.appeals.offline=true
 * to try the "chamber pending" case.
 */
public class SampleChamberClient implements ChamberClient {

    private static final List<Chamber> CHAMBERS = List.of(
        new Chamber("SC1", "Primera Sala Civil", "Cuernavaca"),
        new Chamber("SC2", "Segunda Sala Civil", "Cuernavaca"),
        new Chamber("SCU", "Sala Regional de Cuautla", "Cuautla"),
        new Chamber("SJO", "Sala Regional de Jojutla", "Jojutla"));

    // Kilometers from each court's seat to each chamber's seat, rounded
    private static final Map<String, Map<String, Integer>> DISTANCES = Map.of(
        "01", Map.of("Cuernavaca", 0, "Cuautla", 45, "Jojutla", 45),
        "05", Map.of("Cuernavaca", 30, "Cuautla", 20, "Jojutla", 50),
        "07", Map.of("Cuernavaca", 45, "Cuautla", 70, "Jojutla", 0),
        "12", Map.of("Cuernavaca", 45, "Cuautla", 0, "Jojutla", 70));

    @Override
    public List<Chamber> chambersFor(String courtCode) {
        failWhenOffline();
        Map<String, Integer> distances = DISTANCES.getOrDefault(courtCode, Map.of());
        List<Chamber> ordered = new ArrayList<>(CHAMBERS);
        ordered.sort(Comparator.comparing((Chamber chamber) -> distances.getOrDefault(chamber.getSeat(), 999)));
        return ordered;
    }

    @Override
    public void deliver(CaseFile caseFile, Chamber chamber, String operationKey) {
        failWhenOffline();
    }

    private static void failWhenOffline() {
        if (Boolean.getBoolean("oficialia.appeals.offline")) {
            throw new ServiceUnavailableException("The Tocas system did not answer");
        }
    }
}
