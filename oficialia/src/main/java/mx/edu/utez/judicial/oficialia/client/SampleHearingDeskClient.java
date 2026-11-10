package mx.edu.utez.judicial.oficialia.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;

/**
 * Hands out desks in turn until the Audiencias team publishes its API in contracts/.
 * Start Tomcat with -Doficialia.hearings.offline=true to try the "desk pending" case.
 */
public class SampleHearingDeskClient implements HearingDeskClient {

    private static final List<String> DESKS = List.of("Mesa 1", "Mesa 2", "Mesa 3", "Mesa 4");

    private final AtomicInteger next = new AtomicInteger();

    @Override
    public String assignDesk(String caseFileNumber, String matter) {
        if (Boolean.getBoolean("oficialia.hearings.offline")) {
            throw new ServiceUnavailableException("The Audiencias system did not answer");
        }
        return DESKS.get(Math.floorMod(next.getAndIncrement(), DESKS.size()));
    }
}
