package mx.edu.utez.judicial.oficialia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoginServletTest {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    private final LoginServlet servlet = new LoginServlet();
    private final Map<String, String> headers = new HashMap<>();
    private String forwardedTo;
    private String cacheControlAtForward;

    @Test
    void doesNotLetTheBrowserStoreTheFormWhenItIsOpened() throws Exception {
        servlet.doGet(request(Map.of()), response());

        assertLoginFormIsNotStored();
    }

    @Test
    void doesNotLetTheBrowserStoreTheFormWhenFieldsAreMissing() throws Exception {
        servlet.doPost(request(Map.of("email", "", "password", "")), response());

        assertLoginFormIsNotStored();
    }

    @Test
    void doesNotLetTheBrowserStoreTheFormWhenTheEmailIsMalformed() throws Exception {
        servlet.doPost(request(Map.of("email", "sin-arroba", "password", "secret")), response());

        assertLoginFormIsNotStored();
    }

    @Test
    void doesNotLetTheBrowserStoreTheFormWhenTheCredentialsAreWrong() throws Exception {
        servlet.doPost(request(Map.of("email", "login-servlet-test@utez.edu.mx", "password", "secret")), response());

        assertLoginFormIsNotStored();
    }

    private void assertLoginFormIsNotStored() {
        assertEquals(LOGIN_VIEW, forwardedTo);
        assertEquals("no-store", cacheControlAtForward);
    }

    private HttpServletRequest request(Map<String, String> parameters) {
        RequestDispatcher dispatcher = stub(RequestDispatcher.class, (proxy, method, args) -> {
            cacheControlAtForward = headers.get("Cache-Control");
            return null;
        });
        return stub(HttpServletRequest.class, (proxy, method, args) -> switch (method.getName()) {
            case "getParameter" -> parameters.get((String) args[0]);
            case "getRequestDispatcher" -> {
                forwardedTo = (String) args[0];
                yield dispatcher;
            }
            default -> null;
        });
    }

    private HttpServletResponse response() {
        return stub(HttpServletResponse.class, (proxy, method, args) -> {
            if (method.getName().equals("setHeader")) {
                headers.put((String) args[0], (String) args[1]);
            }
            return null;
        });
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }
}
