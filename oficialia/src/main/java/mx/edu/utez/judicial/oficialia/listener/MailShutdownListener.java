package mx.edu.utez.judicial.oficialia.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.edu.utez.judicial.oficialia.service.MailExecutor;

/**
 * Stops the threads that send e-mail when the application stops, so a redeploy does not leave them running in
 * Tomcat.
 */
@WebListener
public class MailShutdownListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        MailExecutor.shutdown();
    }
}
