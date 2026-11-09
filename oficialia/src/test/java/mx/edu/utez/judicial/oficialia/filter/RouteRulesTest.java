package mx.edu.utez.judicial.oficialia.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RouteRulesTest {

    @Test
    void opensTheExactPublicPaths() {
        assertTrue(RouteRules.isPublic("/login"));
        assertTrue(RouteRules.isPublic("/api/v1/health"));
    }

    @Test
    void opensTheStaticFolders() {
        assertTrue(RouteRules.isPublic("/assets/css/oficialia.css"));
        assertTrue(RouteRules.isPublic("/webjars/bootstrap/5.3.3/css/bootstrap.min.css"));
    }

    @Test
    void doesNotOpenPathsThatOnlyStartLikeAPublicOne() {
        assertFalse(RouteRules.isPublic("/login-history"));
        assertFalse(RouteRules.isPublic("/login/anything"));
        assertFalse(RouteRules.isPublic("/assets"));
    }

    @Test
    void keepsApplicationPagesPrivate() {
        assertFalse(RouteRules.isPublic("/demand-requests"));
        assertFalse(RouteRules.isPublic("/logout"));
    }
}
