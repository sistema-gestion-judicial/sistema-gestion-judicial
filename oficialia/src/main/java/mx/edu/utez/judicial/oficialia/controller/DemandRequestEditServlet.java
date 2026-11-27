package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.filter.AuthFilter;
import mx.edu.utez.judicial.oficialia.model.DemandRequest;
import mx.edu.utez.judicial.oficialia.model.DuplicityCheckForm;
import mx.edu.utez.judicial.oficialia.model.Party;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.service.DemandRequestCorrectionService;

/**
 * Corrects a demand request that was not ruled yet (DFR 3.4). A request already ruled goes back to its detail
 * with the reason.
 */
@WebServlet("/demand-requests/edit")
public class DemandRequestEditServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/demand-requests/edit.jsp";

    private final DemandRequestCorrectionService correctionService = new DemandRequestCorrectionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String folio = request.getParameter("folio");
        try {
            DemandRequest demandRequest = correctionService.getForCorrection(currentUser(request), folio);
            show(request, response, demandRequest, formOf(demandRequest), demandRequest.getLegalRepresentative());
        } catch (BusinessRuleException e) {
            backToDetail(request, response, folio, e.getMessage());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User clerk = currentUser(request);
        String folio = request.getParameter("folio");
        DuplicityCheckForm form = readForm(request);
        String legalRepresentative = request.getParameter("legalRepresentative");
        try {
            DemandRequestCorrectionService.Correction correction = correctionService.correct(clerk, folio, form,
                legalRepresentative, "continue".equals(request.getParameter("action")));
            if (correction.isSaved()) {
                request.getSession().setAttribute(DemandRequestListServlet.FLASH_MESSAGE,
                    "La solicitud " + folio + " se corrigió. El cambio quedó registrado con su usuario y la fecha.");
                response.sendRedirect(detailPath(request, folio));
                return;
            }
            request.setAttribute("errors", correction.errors());
            request.setAttribute("duplicityMatch", correction.warning());
            show(request, response, correctionService.getForCorrection(clerk, folio), form, legalRepresentative);
        } catch (BusinessRuleException e) {
            backToDetail(request, response, folio, e.getMessage());
        } catch (AccessDeniedException e) {
            ErrorView.forbidden(request, response, e.getMessage());
        } catch (NotFoundException e) {
            notFound(request, response, e);
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response, DemandRequest demandRequest,
                      DuplicityCheckForm form, String legalRepresentative) throws ServletException, IOException {
        request.setAttribute("demandRequest", demandRequest);
        request.setAttribute("form", form);
        request.setAttribute("legalRepresentative", legalRepresentative);
        request.setAttribute("matters", correctionService.matters(demandRequest));
        request.setAttribute("maxRepresentativeLength", DemandRequestCorrectionService.MAX_REPRESENTATIVE_LENGTH);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /** A form that starts with what was captured. */
    private static DuplicityCheckForm formOf(DemandRequest demandRequest) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(demandRequest.getMatter());
        Party plaintiff = demandRequest.getPlaintiff();
        form.setPlaintiffAnonymous(!plaintiff.isIdentified());
        form.setPlaintiffType(typeOf(plaintiff));
        form.setPlaintiffName(plaintiff.getName());
        form.setPlaintiffId(plaintiff.getPersonId());
        Party defendant = demandRequest.getDefendant();
        form.setDefendantUnknown(!defendant.isIdentified());
        form.setDefendantType(typeOf(defendant));
        form.setDefendantName(defendant.getName());
        form.setDefendantId(defendant.getPersonId());
        return form;
    }

    private static String typeOf(Party party) {
        return "Persona moral".equals(party.getPersonType()) ? "COMPANY" : "INDIVIDUAL";
    }

    private static DuplicityCheckForm readForm(HttpServletRequest request) {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter(request.getParameter("matter"));
        form.setPlaintiffType(request.getParameter("plaintiffType"));
        form.setPlaintiffName(request.getParameter("plaintiffName"));
        form.setPlaintiffId(request.getParameter("plaintiffId"));
        form.setPlaintiffAnonymous(request.getParameter("plaintiffAnonymous") != null);
        form.setDefendantType(request.getParameter("defendantType"));
        form.setDefendantName(request.getParameter("defendantName"));
        form.setDefendantId(request.getParameter("defendantId"));
        form.setDefendantUnknown(request.getParameter("defendantUnknown") != null);
        return form;
    }

    private static void backToDetail(HttpServletRequest request, HttpServletResponse response, String folio,
                                     String reason) throws IOException {
        request.getSession().setAttribute(DemandRequestListServlet.FLASH_WARNING, reason);
        response.sendRedirect(detailPath(request, folio));
    }

    private static String detailPath(HttpServletRequest request, String folio) {
        return request.getContextPath() + "/demand-requests/detail?folio="
            + URLEncoder.encode(folio == null ? "" : folio, StandardCharsets.UTF_8);
    }

    private static void notFound(HttpServletRequest request, HttpServletResponse response, NotFoundException e)
            throws ServletException, IOException {
        ErrorView.show(request, response, HttpServletResponse.SC_NOT_FOUND, "Solicitud no encontrada", e.getMessage());
    }

    private static User currentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(AuthFilter.SESSION_USER);
    }
}
