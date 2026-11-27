package mx.edu.utez.judicial.oficialia.client;

import java.util.List;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.CaseFile;
import mx.edu.utez.judicial.oficialia.model.Chamber;

/**
 * Talks to the Tocas system for the second instance of a case file (DFR 3.7, #124).
 */
public interface ChamberClient {

    /**
     * The chambers that can take a case file of the court, from the nearest to the farthest.
     *
     * @throws ServiceUnavailableException when the Tocas system does not answer
     */
    List<Chamber> chambersFor(String courtCode);

    /**
     * Hands the case file to the Tocas system with the chosen chamber. The operation key is the same on a
     * retry, so the Tocas system does not open two appeals.
     *
     * @throws ServiceUnavailableException when the Tocas system does not answer
     */
    void deliver(CaseFile caseFile, Chamber chamber, String operationKey);
}
