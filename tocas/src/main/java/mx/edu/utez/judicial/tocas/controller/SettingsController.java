package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.service.HearingService;

import java.io.IOException;

/** "Configuración": read-only profile of the logged-in user. */
@WebServlet("/settings")
public class SettingsController extends BaseController {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var user = currentUser(request);
        request.setAttribute("location", HearingService.location(user.getRoomName()));
        render(request, response, "settings");
    }
}
