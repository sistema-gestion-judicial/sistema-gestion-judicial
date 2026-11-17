<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Distritos judiciales" />
<c:set var="activeNav" value="districts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Distritos judiciales</h1>
    <p class="page-subtitle">Catálogo de distritos a los que se asignan los juzgados del estado.</p>
  </div>
  <div class="d-flex flex-column align-items-end gap-3">
    <a class="btn btn-primary" href="${ctx}/judicial-districts/new">
      <i class="bi bi-plus-lg me-1"></i> Registrar distrito
    </a>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="card-mor p-3 mb-4" method="get" action="${ctx}/judicial-districts">
  <div class="row g-3 align-items-center">
    <div class="col-lg-5">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por clave, nombre o municipio sede"
               aria-label="Buscar por clave, nombre o municipio sede">
      </div>
    </div>
    <div class="col-lg-3">
      <select class="form-select" name="status" aria-label="Estado">
        <option value="">Estado: Todos</option>
        <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Activos</option>
        <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Inactivos</option>
      </select>
    </div>
    <div class="col-lg-4 d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark" href="${ctx}/judicial-districts">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="p-5 text-center">
        <i class="bi bi-geo-alt fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">
          <c:out value="${filtered ? 'No se encontraron distritos judiciales con esos criterios.'
                                   : 'Todavía no hay distritos judiciales registrados.'}" />
        </p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Clave</th>
              <th>Nombre</th>
              <th>Municipio sede</th>
              <th class="text-end">Juzgados</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="district" items="${page.items}">
              <tr>
                <td class="cell-strong"><c:out value="${district.code}" /></td>
                <td><c:out value="${district.name}" /></td>
                <td class="cell-muted"><c:out value="${district.seatMunicipality}" /></td>
                <td class="text-end"><c:out value="${district.courtCount}" /></td>
                <td>
                  <span class="badge-mor ${district.active ? 'status-admissible' : 'status-inadmissible'}">
                    <span class="dot"></span> <c:out value="${district.active ? 'Activo' : 'Inactivo'}" />
                  </span>
                </td>
                <td class="text-end text-nowrap">
                  <c:url var="editUrl" value="/judicial-districts/edit">
                    <c:param name="code" value="${district.code}" />
                  </c:url>
                  <a class="fw-bold text-decoration-none" href="${editUrl}">
                    <i class="bi bi-pencil me-1"></i> Editar
                  </a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <div class="px-3 py-3 border-top d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="text-secondary small">
          <c:out value="${page.firstItem}" />–<c:out value="${page.lastItem}" /> de
          <c:out value="${page.totalItems}" /> distritos judiciales
        </span>
        <c:if test="${page.totalPages > 1}">
          <nav aria-label="Páginas de distritos judiciales">
            <ul class="pagination pagination-sm mb-0">
              <c:url var="previousUrl" value="/judicial-districts">
                <c:param name="q" value="${query}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number - 1}" />
              </c:url>
              <li class="page-item ${page.first ? 'disabled' : ''}">
                <a class="page-link" href="${page.first ? '#' : previousUrl}" aria-label="Anterior">&laquo;</a>
              </li>
              <c:forEach var="number" begin="1" end="${page.totalPages}">
                <c:url var="numberUrl" value="/judicial-districts">
                  <c:param name="q" value="${query}" />
                  <c:param name="status" value="${selectedStatus}" />
                  <c:param name="page" value="${number}" />
                </c:url>
                <li class="page-item ${number == page.number ? 'active' : ''}">
                  <a class="page-link" href="${numberUrl}"
                     ${number == page.number ? 'aria-current="page"' : ''}><c:out value="${number}" /></a>
                </li>
              </c:forEach>
              <c:url var="nextUrl" value="/judicial-districts">
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
