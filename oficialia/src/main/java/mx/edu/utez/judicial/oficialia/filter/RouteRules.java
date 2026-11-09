package mx.edu.utez.judicial.oficialia.filter;

import java.util.List;
import java.util.Set;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * Decides which paths can be reached without signing in and which ones each role can open.
 * Paths must already be resolved by the server, with no ".." segments.
 */
final class RouteRules {

    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/api/v1/health");
    private static final List<String> PUBLIC_FOLDERS = List.of("/assets/", "/webjars/");
    private static final String REQUESTS_PATH = "/demand-requests";
    private static final String NEW_REQUEST_PATH = "/demand-requests/new";

    private RouteRules() {
    }

    static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path) || PUBLIC_FOLDERS.stream().anyMatch(path::startsWith);
    }

    /** Paths without a rule here are open to every signed-in user. */
    static boolean canAccess(String path, UserRole role) {
        if (path.equals(NEW_REQUEST_PATH)) {
            return role == UserRole.CLERK;
        }
        if (path.equals(REQUESTS_PATH) || path.startsWith(REQUESTS_PATH + "/")) {
            return role != UserRole.JUDICIAL_BODY_ADMIN;
        }
        return true;
    }
}
