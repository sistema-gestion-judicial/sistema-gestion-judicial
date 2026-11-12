<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Juzgados" />
<c:set var="activeNav" value="courts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Juzgados</h1>
    <p class="page-subtitle">Catálogo de juzgados del estado, con su distrito y las materias que atienden.</p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty courts}">
      <div class="p-5 text-center">
        <i class="bi bi-bank fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">Todavía no hay juzgados registrados.</p>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Clave</th>
              <th>Número</th>
              <th>Nombre</th>
              <th>Distrito judicial</th>
              <th>Materias</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="court" items="${courts}">
              <tr>
                <td class="cell-strong"><c:out value="${court.code}" /></td>
                <td class="cell-muted"><c:out value="${court.number}" /></td>
                <td><c:out value="${court.name}" /></td>
                <td class="cell-muted"><c:out value="${court.judicialDistrict}" /></td>
                <td class="cell-muted">
                  <c:forEach var="matterCode" items="${court.subjectMatterCodes}" varStatus="loop">
                    <c:out value="${matterCode}" /><c:if test="${not loop.last}">, </c:if>
                  </c:forEach>
                </td>
                <td>
                  <span class="badge-mor ${court.active ? 'status-admissible' : 'status-inadmissible'}">
                    <span class="dot"></span> <c:out value="${court.active ? 'Activo' : 'Inactivo'}" />
                  </span>
                </td>
                <td class="text-end text-nowrap">
                  <c:url var="editUrl" value="/courts/edit">
                    <c:param name="code" value="${court.code}" />
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
      <div class="px-3 py-3 border-top text-secondary small">
        <c:out value="${courts.size()}" /> juzgados
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
