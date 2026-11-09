package mx.edu.utez.judicial.oficialia.filter;

import java.util.List;
import java.util.Set;

/**
 * Decides which paths can be reached without signing in.
 * Paths must already be resolved by the server, with no ".." segments.
 */
final class RouteRules {

    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/api/v1/health");
    private static final List<String> PUBLIC_FOLDERS = List.of("/assets/", "/webjars/");

    private RouteRules() {
    }

    static boolean isPublic(String path) {
        return PUBLIC_PATHS.contains(path) || PUBLIC_FOLDERS.stream().anyMatch(path::startsWith);
    }
}
