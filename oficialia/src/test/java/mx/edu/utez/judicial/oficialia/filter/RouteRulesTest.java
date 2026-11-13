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
    void opensThePasswordRecoveryPagesForPeopleWhoCannotSignIn() {
        assertTrue(RouteRules.isPublic("/password-recovery"));
        assertTrue(RouteRules.isPublic("/password-recovery/code"));
        assertTrue(RouteRules.isPublic("/password-recovery/reset"));
    }

    @Test
    void opensTheSystemApiGuardedByItsKeyFilter() {
        assertTrue(RouteRules.isPublic("/api/v1/courts"));
    }

    @Test
    void keepsApiPathsOutsideTheKeyFilterPrivate() {
        assertFalse(RouteRules.isPublic("/api"));
        assertFalse(RouteRules.isPublic("/api/v2/x"));
    }

    @Test
    void doesNotOpenPathsThatOnlyStartLikeAPublicOne() {
        assertFalse(RouteRules.isPublic("/login-history"));
        assertFalse(RouteRules.isPublic("/login/anything"));
        assertFalse(RouteRules.isPublic("/assets"));
        assertFalse(RouteRules.isPublic("/password-recovery/other"));
        assertFalse(RouteRules.isPublic("/password-recovery-admin"));
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
    }

    @Test
    void courtStaffSeeRequests() {
        assertTrue(RouteRules.canAccess("/demand-requests", UserRole.CLERK));
        assertTrue(RouteRules.canAccess("/demand-requests/detail", UserRole.COURT_ADMIN));
    }

    @Test
    void everyRoleReachesTheHomePage() {
        for (UserRole role : UserRole.values()) {
            assertTrue(RouteRules.canAccess("/home", role));
        }
    }

    // DEMO (temporary): the judicial body administrator works as the Oficial de Partes
    @Test
    void theJudicialBodyAdministratorReachesEverythingTheClerkReaches() {
        assertTrue(RouteRules.canAccess("/demand-requests", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/demand-requests/detail", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/demand-requests/new", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/demand-requests/rule", UserRole.JUDICIAL_BODY_ADMIN));
        assertTrue(RouteRules.canAccess("/logout", UserRole.JUDICIAL_BODY_ADMIN));
    }

    @Test
    void otherRolesKeepPathsWithoutARuleOpen() {
        assertTrue(RouteRules.canAccess("/any-future-page", UserRole.COURT_ADMIN));
        assertTrue(RouteRules.canAccess("/demand-requests-report", UserRole.CLERK));
    }
}
