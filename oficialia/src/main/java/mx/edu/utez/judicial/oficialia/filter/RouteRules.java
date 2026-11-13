package mx.edu.utez.judicial.oficialia.filter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mx.edu.utez.judicial.oficialia.model.UserRole;

/**
 * Decides which paths can be reached without signing in and which ones each role can open.
 * Paths must already be resolved by the server, with no ".." segments.
 */
final class RouteRules {

    // The password recovery pages are for people who cannot sign in
    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/password-recovery",
        "/password-recovery/code", "/password-recovery/reset", "/api/v1/health");
    // The system API skips the session check because ApiKeyFilter guards /api/v1/* with X-Api-Key
    private static final List<String> PUBLIC_FOLDERS = List.of("/assets/", "/webjars/", "/api/v1/");
    private static final String NEW_REQUEST_PATH = "/demand-requests/new";
    // The catalogs are managed only by the judicial body administrator (DFR 2.1)
    private static final Set<String> CATALOG_PATHS = Set.of("/judicial-districts", "/judicial-districts/new");
    // "/" and "/index.jsp" only redirect to the home page
    private static final Set<String> JUDICIAL_BODY_ADMIN_PATHS = Stream.concat(
        Stream.of("/home", "/logout", "/", "/index.jsp"), CATALOG_PATHS.stream()).collect(Collectors.toSet());

    private RouteRules() {
    }

    static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path) || PUBLIC_FOLDERS.stream().anyMatch(path::startsWith);
    }

    /**
     * Paths without a rule here are open to every signed-in user.
     */
    static boolean canAccess(String path, UserRole role) {
        // DEMO (temporary): the judicial body administrator reaches every page the Oficial de Partes reaches
        if (path.equals(NEW_REQUEST_PATH)) {
            return role == UserRole.CLERK || role == UserRole.JUDICIAL_BODY_ADMIN;
        }
        if (CATALOG_PATHS.contains(path)) {
            return false;
        }
        return true;
    }
}
