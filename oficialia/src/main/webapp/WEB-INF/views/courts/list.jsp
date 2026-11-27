<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Juzgados" />
<c:set var="activeNav" value="courts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Juzgados</h1>
    <p class="page-subtitle">Catálogo de juzgados del estado: dirección, distrito y materias que atienden.</p>
  </div>
  <c:if test="${canRegister}">
    <div class="d-flex flex-column align-items-end gap-3">
      <a class="btn btn-primary" href="${ctx}/courts/new">
        <i class="bi bi-plus-lg me-1"></i> Registrar juzgado
      </a>
    </div>
  </c:if>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<form class="filter-bar" method="get" action="${ctx}/courts">
  <div class="row g-3 align-items-center">
    <div class="col-lg">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Clave, número, nombre o municipio"
               aria-label="Buscar por clave, número, nombre o municipio">
      </div>
    </div>
    <div class="col-lg-2">
      <select class="form-select" name="district" aria-label="Distrito judicial">
        <option value="">Distrito: Todos</option>
        <c:forEach var="district" items="${districts}">
          <option value="<c:out value='${district.code}' />" ${selectedDistrict == district.code ? 'selected' : ''}>
            <c:out value="${district.name}" />
          </option>
        </c:forEach>
      </select>
    </div>
    <div class="col-lg-auto">
      <select class="form-select" name="matter" aria-label="Materia">
        <option value="">Materia: Todas</option>
        <c:forEach var="matter" items="${subjectMatters}">
          <option value="<c:out value='${matter.code}' />" ${selectedMatter == matter.code ? 'selected' : ''}>
            <c:out value="${matter.name}" />
          </option>
        </c:forEach>
      </select>
    </div>
    <c:if test="${canSeeInactive}">
      <div class="col-lg-auto">
        <select class="form-select" name="status" aria-label="Estado">
          <option value="">Estado: Todos</option>
          <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Activos</option>
          <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Inactivos</option>
        </select>
      </div>
    </c:if>
    <div class="col-lg-auto d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark text-nowrap" href="${ctx}/courts">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary text-nowrap">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Juzgados del estado</h2>
      <p class="card-mor-subtitle">Dirección, distrito judicial y materias de cada juzgado.</p>
    </div>
    <p class="card-mor-count"><c:out value="${page.totalItems}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <c:choose>
          <c:when test="${filtered}">
            <p class="empty-state-title">No hay juzgados con esos filtros</p>
            <p class="empty-state-text">Pruebe con otra clave, distrito o materia, o limpie los filtros.</p>
            <a class="btn btn-outline-primary" href="${ctx}/courts">Limpiar filtros</a>
          </c:when>
          <c:otherwise>
            <p class="empty-state-title">Todavía no hay juzgados registrados</p>
          </c:otherwise>
        </c:choose>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Clave</th>
              <th>Juzgado</th>
              <th>Dirección</th>
              <th>Materias</th>
              <c:if test="${canSeeInactive}">
                <th>Estado</th>
              </c:if>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="court" items="${page.items}">
              <tr>
                <td>
                  <span class="cell-strong"><c:out value="${court.code}" /></span>
                  <span class="cell-sub">Número <c:out value="${court.number}" /></span>
                </td>
                <td>
                  <c:out value="${court.name}" />
                  <span class="cell-sub"><c:out value="${court.judicialDistrict}" /></span>
                </td>
                <td>
                  <c:out value="${empty court.fullAddress ? '—' : court.fullAddress}" />
                  <c:if test="${not empty court.phone}">
                    <span class="cell-sub">Tel. <c:out value="${court.phone}" /></span>
                  </c:if>
                </td>
                <td><c:out value="${empty court.subjectMatters ? '—' : court.subjectMatters}" /></td>
                <c:if test="${canSeeInactive}">
                  <td>
                    <span class="badge-mor ${court.active ? 'status-admissible' : 'status-inadmissible'}">
                      <span class="dot"></span> <c:out value="${court.active ? 'Activo' : 'Inactivo'}" />
                    </span>
                  </td>
                </c:if>
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
          <nav aria-label="Páginas de juzgados">
            <ul class="pagination mb-0">
              <c:url var="previousUrl" value="/courts">
                <c:param name="q" value="${query}" />
                <c:param name="district" value="${selectedDistrict}" />
                <c:param name="matter" value="${selectedMatter}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number - 1}" />
              </c:url>
              <li class="page-item ${page.first ? 'disabled' : ''}">
                <a class="page-link" href="${page.first ? '#' : previousUrl}" aria-label="Anterior">&laquo;</a>
              </li>
              <c:forEach var="number" begin="1" end="${page.totalPages}">
                <c:url var="numberUrl" value="/courts">
                  <c:param name="q" value="${query}" />
                  <c:param name="district" value="${selectedDistrict}" />
                  <c:param name="matter" value="${selectedMatter}" />
                  <c:param name="status" value="${selectedStatus}" />
                  <c:param name="page" value="${number}" />
                </c:url>
                <li class="page-item ${number == page.number ? 'active' : ''}">
                  <a class="page-link" href="${numberUrl}"
                     ${number == page.number ? 'aria-current="page"' : ''}><c:out value="${number}" /></a>
                </li>
              </c:forEach>
              <c:url var="nextUrl" value="/courts">
                <c:param name="q" value="${query}" />
                <c:param name="district" value="${selectedDistrict}" />
                <c:param name="matter" value="${selectedMatter}" />
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
