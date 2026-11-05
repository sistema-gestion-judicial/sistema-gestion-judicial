<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Solicitudes de demanda" />
<c:set var="activeNav" value="requests" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Solicitudes de demanda</h1>
    <p class="page-subtitle">
      Consulte y califique las solicitudes recibidas en la Oficialía de Partes de
      <c:out value="${sessionScope.currentUser.courtName}" />.
    </p>
  </div>
  <div class="d-flex flex-column align-items-end gap-3">
    <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
    <a class="btn btn-primary" href="${ctx}/demand-requests/new">
      <i class="bi bi-plus-lg me-1"></i> Registrar solicitud
    </a>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-info d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-info-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="card-mor p-3 mb-4" method="get" action="${ctx}/demand-requests">
  <div class="row g-3 align-items-center">
    <div class="col-lg-5">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por folio o por partes" aria-label="Buscar por folio o por partes">
      </div>
    </div>
    <div class="col-lg-3">
      <select class="form-select" name="status" aria-label="Estado">
        <option value="">Estado: Todos</option>
        <c:forEach var="status" items="${statuses}">
          <option value="${status.code}" ${selectedStatus == status.code ? 'selected' : ''}>
            <c:out value="${status.label}" />
          </option>
        </c:forEach>
      </select>
    </div>
    <div class="col-lg-4 d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark" href="${ctx}/demand-requests">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty demandRequests}">
      <div class="p-5 text-center">
        <i class="bi bi-inbox fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">No se encontraron solicitudes con esos criterios.</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Folio</th>
              <th>Fecha de recepción</th>
              <th>Materia</th>
              <th>Partes</th>
              <th>Expediente</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="demandRequest" items="${demandRequests}">
              <tr>
                <td class="cell-strong"><c:out value="${demandRequest.folio}" /></td>
                <td><c:out value="${demandRequest.receivedDateText}" /></td>
                <td class="cell-muted"><c:out value="${demandRequest.matter}" /></td>
                <td class="cell-muted"><c:out value="${demandRequest.partiesText}" /></td>
                <td class="cell-muted"><c:out value="${demandRequest.caseFileText}" /></td>
                <td>
                  <span class="badge-mor ${demandRequest.status.cssClass}">
                    <span class="dot"></span> <c:out value="${demandRequest.status.label}" />
                  </span>
                </td>
                <td class="text-end">
                  <span class="text-secondary fw-bold" title="El detalle se muestra en un siguiente avance">
                    <i class="bi bi-eye me-1"></i> Ver
                  </span>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <div class="px-3 py-3 border-top text-secondary small">
        Mostrando <c:out value="${demandRequests.size()}" /> de <c:out value="${totalCount}" /> solicitudes
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
