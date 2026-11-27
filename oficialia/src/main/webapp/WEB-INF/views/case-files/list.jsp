<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Expedientes" />
<c:set var="activeNav" value="case-files" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Expedientes</h1>
    <p class="page-subtitle">
      Expedientes de <c:out value="${sessionScope.currentUser.courtName}" /> abiertos por solicitudes procedentes.
    </p>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="filter-bar" method="get" action="${ctx}/case-files">
  <div class="row g-3 align-items-center">
    <div class="col-lg-5">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por número, parte o materia" aria-label="Buscar por número, parte o materia">
      </div>
    </div>
    <div class="col-lg-3">
      <select class="form-select" name="status" aria-label="Estado">
        <option value="">Estado: Todos</option>
        <c:forEach var="status" items="${statuses}">
          <option value="${status}" ${selectedStatus == status.name() ? 'selected' : ''}><c:out value="${status.label}" /></option>
        </c:forEach>
      </select>
    </div>
    <div class="col-lg-4 d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark" href="${ctx}/case-files">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Expedientes del juzgado</h2>
      <p class="card-mor-subtitle">Número, partes, materia, mesa, fecha y estado de cada expediente.</p>
    </div>
    <p class="card-mor-count"><c:out value="${page.totalItems}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <c:choose>
          <c:when test="${filtered}">
            <p class="empty-state-title">No se encontraron expedientes</p>
            <p class="empty-state-text">Pruebe con otro número, parte o estado, o limpie los filtros.</p>
            <a class="btn btn-outline-primary" href="${ctx}/case-files">Limpiar filtros</a>
          </c:when>
          <c:otherwise>
            <p class="empty-state-title">Todavía no hay expedientes</p>
            <p class="empty-state-text">Se abren al calificar una solicitud como procedente.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Expediente</th>
              <th>Partes</th>
              <th>Materia</th>
              <th>Mesa</th>
              <th>Fecha</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="caseFile" items="${page.items}">
              <c:url var="detailUrl" value="/case-files/detail">
                <c:param name="number" value="${caseFile.number}" />
              </c:url>
              <tr>
                <td>
                  <span class="cell-strong"><c:out value="${caseFile.number}" /></span>
                  <span class="cell-sub">Folio <c:out value="${caseFile.folio}" /></span>
                </td>
                <td><c:out value="${caseFile.partiesText}" /></td>
                <td><c:out value="${caseFile.matter}" /></td>
                <td><c:out value="${empty caseFile.deskName ? 'Pendiente' : caseFile.deskName}" /></td>
                <td><c:out value="${caseFile.openedAtText}" /></td>
                <td>
                  <span class="badge-mor ${caseFile.status.cssClass}">
                    <span class="dot"></span> <c:out value="${caseFile.status.label}" />
                  </span>
                  <c:if test="${caseFile.concluded}">
                    <span class="cell-sub">El <c:out value="${caseFile.concludedAtText}" /></span>
                  </c:if>
                </td>
                <td class="text-end text-nowrap">
                  <a class="row-action" href="<c:out value='${detailUrl}' />"
                     aria-label="Ver expediente <c:out value='${caseFile.number}' />">
                    <i class="bi bi-eye" aria-hidden="true"></i> Ver
                  </a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <div class="card-mor-footer">
        <span>
          Mostrando <c:out value="${page.firstItem}" />–<c:out value="${page.lastItem}" />
          de <c:out value="${page.totalItems}" />
        </span>
        <c:if test="${page.totalPages > 1}">
          <nav aria-label="Páginas de expedientes">
            <ul class="pagination mb-0">
              <c:url var="previousUrl" value="/case-files">
                <c:param name="q" value="${query}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number - 1}" />
              </c:url>
              <li class="page-item ${page.first ? 'disabled' : ''}">
                <a class="page-link" href="${page.first ? '#' : previousUrl}" aria-label="Anterior">&laquo;</a>
              </li>
              <c:forEach var="number" begin="1" end="${page.totalPages}">
                <c:url var="numberUrl" value="/case-files">
                  <c:param name="q" value="${query}" />
                  <c:param name="status" value="${selectedStatus}" />
                  <c:param name="page" value="${number}" />
                </c:url>
                <li class="page-item ${number == page.number ? 'active' : ''}">
                  <a class="page-link" href="${numberUrl}"
                     ${number == page.number ? 'aria-current="page"' : ''}><c:out value="${number}" /></a>
                </li>
              </c:forEach>
              <c:url var="nextUrl" value="/case-files">
                <c:param name="q" value="${query}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number + 1}" />
              </c:url>
              <li class="page-item ${page.last ? 'disabled' : ''}">
                <a class="page-link" href="${page.last ? '#' : nextUrl}" aria-label="Siguiente">&raquo;</a>
              </li>
            </ul>
          </nav>
        </c:if>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
