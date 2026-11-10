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
    // The system API skips the session check because ApiKeyFilter guards /api/v1/* with X-Api-Key
    private static final List<String> PUBLIC_FOLDERS = List.of("/assets/", "/webjars/", "/api/v1/");
    private static final String NEW_REQUEST_PATH = "/demand-requests/new";
    // "/" and "/index.jsp" only redirect to the home page
    private static final Set<String> JUDICIAL_BODY_ADMIN_PATHS = Set.of("/home", "/logout", "/", "/index.jsp");

    private RouteRules() {
    }

    static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path) || PUBLIC_FOLDERS.stream().anyMatch(path::startsWith);
    }

    /**
     * The judicial body administrator only reaches an explicit list of paths. For the other
     * roles, paths without a rule here are open to every signed-in user.
     */
    static boolean canAccess(String path, UserRole role) {
        if (role == UserRole.JUDICIAL_BODY_ADMIN) {
            return JUDICIAL_BODY_ADMIN_PATHS.contains(path);
        }
        if (path.equals(NEW_REQUEST_PATH)) {
            return role == UserRole.CLERK;
        }
        return true;
    }
}
