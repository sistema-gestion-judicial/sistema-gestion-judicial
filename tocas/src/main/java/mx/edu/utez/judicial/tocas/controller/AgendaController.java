package mx.edu.utez.judicial.tocas.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.judicial.tocas.model.User;
import mx.edu.utez.judicial.tocas.service.AppClock;
import mx.edu.utez.judicial.tocas.service.FormatUtils;
import mx.edu.utez.judicial.tocas.service.HearingService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Monthly calendar of hearings. Params: month=yyyy-MM, date=yyyy-MM-dd. */
@WebServlet("/agenda")
public class AgendaController extends BaseController {
    private final HearingService hearingService = new HearingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        LocalDate today = AppClock.today();
        LocalDate selected = dateParam(request, "date");
        LocalDate month = today.withDayOfMonth(1);
        try {
            month = LocalDate.parse(stringParam(request, "month") + "-01");
        } catch (DateTimeParseException e) {
            if (selected != null) month = selected.withDayOfMonth(1);
        }
        if (selected != null && !selected.withDayOfMonth(1).equals(month)) selected = null;
        LocalDate next = month.plusMonths(1);

        request.setAttribute("monthStart", month);
        request.setAttribute("prevMonth", FormatUtils.isoMonth(month.minusMonths(1)));
        request.setAttribute("nextMonth", FormatUtils.isoMonth(next));
        request.setAttribute("todayMonth", FormatUtils.isoMonth(today));
        request.setAttribute("days", hearingService.month(user, month, selected));
        request.setAttribute("selectedDate", selected);
        request.setAttribute("upcoming", hearingService.upcoming(user, 5));
        request.setAttribute("upcomingCount", hearingService.countWithinDays(user, 15));
        if (selected != null) request.setAttribute("dayHearings", hearingService.onDate(user, selected));

        int nextCount = hearingService.countInMonth(user, next);
        if (nextCount > 0) {
            request.setAttribute("nextMonthCount", nextCount);
            request.setAttribute("nextMonthLabel", FormatUtils.monthName(next));
            request.setAttribute("nextMonthDays", HearingService.joinDays(hearingService.daysWithHearings(user, next)));
        }
        render(request, response, "agenda");
    }
}
