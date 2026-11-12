package mx.edu.utez.judicial.oficialia.client;

import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;

/**
 * Asks the Audiencias system for the desk that will hold the first hearing of a new case file (#30).
 */
public interface HearingDeskClient {

    /**
     * Returns the name of the assigned desk, for example "Mesa 2".
     *
     * @throws ServiceUnavailableException when the Audiencias system does not answer
     */
    String assignDesk(String caseFileNumber, String matter);
}
