package mx.edu.utez.judicial.oficialia.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.edu.utez.judicial.oficialia.model.UserRole;
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

    @Test
    void onlyTheClerkRegistersRequests() {
        assertTrue(RouteRules.canAccess("/demand-requests/new", UserRole.CLERK));
        assertFalse(RouteRules.canAccess("/demand-requests/new", UserRole.COURT_ADMIN));
        assertFalse(RouteRules.canAccess("/demand-requests/new", UserRole.JUDICIAL_BODY_ADMIN));
    }

    @Test
    void courtStaffSeeRequests() {
        assertTrue(RouteRules.canAccess("/demand-requests", UserRole.CLERK));
        assertTrue(RouteRules.canAccess("/demand-requests/detail", UserRole.COURT_ADMIN));
    }

    @Test
    void theJudicialBodyAdministratorDoesNotSeeRequests() {
        assertFalse(RouteRules.canAccess("/demand-requests", UserRole.JUDICIAL_BODY_ADMIN));
        assertFalse(RouteRules.canAccess("/demand-requests/detail", UserRole.JUDICIAL_BODY_ADMIN));
    }

    @Test
    void everyRoleReachesTheHomePage() {
        for (UserRole role : UserRole.values()) {
            assertTrue(RouteRules.canAccess("/home", role));
        }
    }

    @Test
    void theJudicialBodyAdministratorReachesHomeSignOutAndTheEntryRedirect() {
        assertTrue(RouteRules.canAccess("/logout", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/index.jsp", UserRole.JUDICIAL_BODY_ADMIN));
    }

    @Test
    void theJudicialBodyAdministratorReachesNothingElse() {
        assertFalse(RouteRules.canAccess("/demand-requests/new", UserRole.JUDICIAL_BODY_ADMIN));
        assertFalse(RouteRules.canAccess("/demand-requests-report", UserRole.JUDICIAL_BODY_ADMIN));
        assertFalse(RouteRules.canAccess("/any-future-page", UserRole.JUDICIAL_BODY_ADMIN));
    }

    @Test
    void otherRolesKeepPathsWithoutARuleOpen() {
        assertTrue(RouteRules.canAccess("/any-future-page", UserRole.COURT_ADMIN));
        assertTrue(RouteRules.canAccess("/demand-requests-report", UserRole.CLERK));
    }
}
