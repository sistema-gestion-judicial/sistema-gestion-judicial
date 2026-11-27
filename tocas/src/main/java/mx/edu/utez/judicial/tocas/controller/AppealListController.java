package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.service.AppealService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** "Mis tocas": search, filters and pagination. */
@WebServlet("/tocas")
public class AppealListController extends BaseController {
    private final AppealService appealService = new AppealService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = stringParam(request, "q");
        String status = stringParam(request, "status");
        if (!status.equals("NEW") && !status.equals("CONCLUDED")) status = "";
        LocalDate from = dateParam(request, "from");
        LocalDate to = dateParam(request, "to");
        int page = intParam(request, "page", 1);

        request.setAttribute("result", appealService.search(currentUser(request), query, status, from, to, page));
        request.setAttribute("q", query);
        request.setAttribute("status", status);
        request.setAttribute("from", from);
        request.setAttribute("to", to);

        StringBuilder qs = new StringBuilder();
        qs.append("q=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
        qs.append("&status=").append(status);
        qs.append("&from=").append(from == null ? "" : from.toString());
        qs.append("&to=").append(to == null ? "" : to.toString());
        request.setAttribute("queryBase", qs.toString());
        render(request, response, "appeals/list");
    }
}
