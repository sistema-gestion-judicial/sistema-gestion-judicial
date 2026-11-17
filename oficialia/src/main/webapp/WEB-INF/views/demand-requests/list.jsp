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
    <c:if test="${sessionScope.currentUser.clerk}">
      <a class="btn btn-primary" href="${ctx}/demand-requests/new">
        <i class="bi bi-plus-lg me-1"></i> Registrar solicitud
      </a>
    </c:if>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-info d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-info-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="filter-bar" method="get" action="${ctx}/demand-requests">
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
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Solicitudes recibidas</h2>
      <p class="card-mor-subtitle">Consulta y calificación de las solicitudes de demanda del juzgado.</p>
    </div>
    <p class="card-mor-count"><c:out value="${totalCount}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty demandRequests}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <p class="empty-state-title">No hay solicitudes con esos filtros</p>
        <p class="empty-state-text">Pruebe con otro folio, parte o estado, o limpie los filtros.</p>
        <a class="btn btn-outline-primary" href="${ctx}/demand-requests">Limpiar filtros</a>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Folio</th>
              <th>Partes</th>
              <th>Materia</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="demandRequest" items="${demandRequests}">
              <tr>
                <td>
                  <span class="cell-strong"><c:out value="${demandRequest.folio}" /></span>
                  <span class="cell-sub">Recepción: <c:out value="${demandRequest.receivedDateText}" /></span>
                </td>
                <td>
                  <c:out value="${demandRequest.plaintiff.displayName}" />
                  <span class="cell-sub">vs. <c:out value="${demandRequest.defendant.displayName}" /></span>
                </td>
                <td>
                  <c:out value="${demandRequest.matter}" />
                  <span class="cell-sub">Expediente: <c:out value="${demandRequest.caseFileText}" /></span>
                </td>
                <td>
                  <span class="badge-mor ${demandRequest.status.cssClass}">
                    <span class="dot"></span> <c:out value="${demandRequest.status.label}" />
                  </span>
                </td>
                <td class="text-end text-nowrap">
                  <c:url var="detailUrl" value="/demand-requests/detail">
                    <c:param name="folio" value="${demandRequest.folio}" />
                  </c:url>
                  <a class="row-action" href="${detailUrl}"
                     aria-label="Ver solicitud ${demandRequest.folio}">
                    <span class="icon-eye" aria-hidden="true"></span> Ver
                  </a>
                  <c:if test="${demandRequest.pending and sessionScope.currentUser.clerk}">
                    <c:url var="ruleUrl" value="/demand-requests/rule">
                      <c:param name="folio" value="${demandRequest.folio}" />
                    </c:url>
                    <a class="row-action" href="${ruleUrl}"
                       aria-label="Calificar solicitud ${demandRequest.folio}">
                      <i class="bi bi-clipboard-check" aria-hidden="true"></i> Calificar
                    </a>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:otherwise>
  </c:choose>
  <div class="card-mor-footer">
    Mostrando <c:out value="${demandRequests.size()}" /> de <c:out value="${totalCount}" />
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
