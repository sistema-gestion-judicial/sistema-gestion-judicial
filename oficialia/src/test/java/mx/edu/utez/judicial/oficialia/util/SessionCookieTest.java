package mx.edu.utez.judicial.oficialia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SessionCookieTest {

    @Test
    void usesTheContextPathOfTheApplication() {
        assertEquals("/oficialia", SessionCookie.pathFor("/oficialia"));
    }

    @Test
    void usesTheRootPathWhenTheApplicationIsAtTheRoot() {
        assertEquals("/", SessionCookie.pathFor(""));
    }
}
