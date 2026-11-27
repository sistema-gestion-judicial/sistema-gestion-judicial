<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Materias" />
<c:set var="activeNav" value="matters" />
<c:set var="pageScript" value="status-confirm.js" />
<c:set var="statusAction" value="/subject-matters/status" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Materias</h1>
    <p class="page-subtitle">Catálogo único de materias que usan las solicitudes, los juzgados y los demás sistemas.</p>
  </div>
  <div class="d-flex flex-column align-items-end gap-3">
    <a class="btn btn-primary" href="${ctx}/subject-matters/new">
      <i class="bi bi-plus-lg me-1"></i> Registrar materia
    </a>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="filter-bar" method="get" action="${ctx}/subject-matters">
  <div class="row g-3 align-items-center">
    <div class="col-lg-5">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por clave, nombre o descripción"
               aria-label="Buscar por clave, nombre o descripción">
      </div>
    </div>
    <div class="col-lg-3">
      <select class="form-select" name="status" aria-label="Estado">
        <option value="">Estado: Todos</option>
        <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Activas</option>
        <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Inactivas</option>
      </select>
    </div>
    <div class="col-lg-4 d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark" href="${ctx}/subject-matters">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Materias del catálogo</h2>
      <p class="card-mor-subtitle">Clave, nombre y número de juzgados que atienden cada materia.</p>
    </div>
    <p class="card-mor-count"><c:out value="${page.totalItems}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <c:choose>
          <c:when test="${filtered}">
            <p class="empty-state-title">No se encontraron materias con esos criterios</p>
            <p class="empty-state-text">Pruebe con otra clave, nombre o estado, o limpie los filtros.</p>
            <a class="btn btn-outline-primary" href="${ctx}/subject-matters">Limpiar filtros</a>
          </c:when>
          <c:otherwise>
            <p class="empty-state-title">Todavía no hay materias</p>
            <p class="empty-state-text">Registre la primera materia para clasificar las solicitudes.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Materia</th>
              <th>Descripción</th>
              <th class="text-end">Juzgados</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="matter" items="${page.items}">
              <tr>
                <td>
                  <span class="cell-strong"><c:out value="${matter.name}" /></span>
                  <span class="cell-sub">Clave: <c:out value="${matter.code}" /></span>
                </td>
                <td><c:out value="${empty matter.description ? '—' : matter.description}" /></td>
                <td class="text-end"><c:out value="${matter.courtCount}" /></td>
                <td>
                  <span class="badge-mor ${matter.active ? 'status-admissible' : 'status-inadmissible'}">
                    <span class="dot"></span> <c:out value="${matter.active ? 'Activa' : 'Inactiva'}" />
                  </span>
                </td>
                <td class="text-end text-nowrap">
                  <c:url var="editUrl" value="/subject-matters/edit">
                    <c:param name="code" value="${matter.code}" />
                  </c:url>
                  <a class="row-action" href="<c:out value='${editUrl}' />"
                     aria-label="Editar materia <c:out value='${matter.name}' />">
                    <i class="bi bi-pencil" aria-hidden="true"></i> Editar
                  </a>
                  <button type="button" class="row-action border-0 bg-transparent" data-status-toggle
                          data-code="<c:out value='${matter.code}' />" data-name="<c:out value='${matter.name}' />"
                          data-active="${not matter.active}" data-label="la materia"
                          data-title-on="Activar materia" data-title-off="Desactivar materia"
                          data-note-on="Volverá a ofrecerse en los formularios de solicitud y de juzgado."
                          data-note-off="Dejará de ofrecerse en los formularios de solicitud y de juzgado. Las solicitudes ya registradas la seguirán mostrando.">
                    <i class="bi ${matter.active ? 'bi-toggle-on' : 'bi-toggle-off'}" aria-hidden="true"></i>
                    <c:out value="${matter.active ? 'Desactivar' : 'Activar'}" />
                  </button>
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
          <nav aria-label="Páginas de materias">
            <ul class="pagination mb-0">
              <c:url var="previousUrl" value="/subject-matters">
                <c:param name="q" value="${query}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number - 1}" />
              </c:url>
              <li class="page-item ${page.first ? 'disabled' : ''}">
                <a class="page-link" href="${page.first ? '#' : previousUrl}" aria-label="Anterior">&laquo;</a>
              </li>
              <c:forEach var="number" begin="1" end="${page.totalPages}">
                <c:url var="numberUrl" value="/subject-matters">
                  <c:param name="q" value="${query}" />
                  <c:param name="status" value="${selectedStatus}" />
                  <c:param name="page" value="${number}" />
                </c:url>
                <li class="page-item ${number == page.number ? 'active' : ''}">
                  <a class="page-link" href="${numberUrl}"
                     ${number == page.number ? 'aria-current="page"' : ''}><c:out value="${number}" /></a>
                </li>
              </c:forEach>
              <c:url var="nextUrl" value="/subject-matters">
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

<%@ include file="/WEB-INF/views/includes/status-modal.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
