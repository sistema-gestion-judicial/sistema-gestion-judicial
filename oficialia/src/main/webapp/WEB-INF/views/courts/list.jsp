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
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<form class="card-mor p-3 mb-4" method="get" action="${ctx}/courts">
  <div class="row g-3 align-items-center">
    <div class="col-lg-4">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por clave, número, nombre o municipio"
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
    <div class="col-lg-2">
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
      <div class="col-lg-2">
        <select class="form-select" name="status" aria-label="Estado">
          <option value="">Estado: Todos</option>
          <option value="active" ${selectedStatus == 'active' ? 'selected' : ''}>Activos</option>
          <option value="inactive" ${selectedStatus == 'inactive' ? 'selected' : ''}>Inactivos</option>
        </select>
      </div>
    </c:if>
    <div class="col-lg d-flex justify-content-lg-end align-items-center gap-3">
      <a class="link-dark text-nowrap" href="${ctx}/courts">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary text-nowrap">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="p-5 text-center">
        <i class="bi bi-bank fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">
          <c:out value="${filtered ? 'No se encontraron juzgados con esos criterios.'
                                   : 'Todavía no hay juzgados registrados.'}" />
        </p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Clave</th>
              <th class="text-end">Número</th>
              <th>Nombre</th>
              <th>Distrito judicial</th>
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
                <td class="cell-strong"><c:out value="${court.code}" /></td>
                <td class="text-end"><c:out value="${court.number}" /></td>
                <td><c:out value="${court.name}" /></td>
                <td class="cell-muted"><c:out value="${court.judicialDistrict}" /></td>
                <td class="cell-muted">
                  <c:out value="${empty court.fullAddress ? '—' : court.fullAddress}" />
                  <c:if test="${not empty court.phone}">
                    <div class="small"><i class="bi bi-telephone me-1"></i><c:out value="${court.phone}" /></div>
                  </c:if>
                </td>
                <td class="cell-muted"><c:out value="${empty court.subjectMatters ? '—' : court.subjectMatters}" /></td>
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
      <div class="px-3 py-3 border-top d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="text-secondary small">
          <c:out value="${page.firstItem}" />–<c:out value="${page.lastItem}" /> de
          <c:out value="${page.totalItems}" /> juzgados
        </span>
        <c:if test="${page.totalPages > 1}">
          <nav aria-label="Páginas de juzgados">
            <ul class="pagination pagination-sm mb-0">
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
