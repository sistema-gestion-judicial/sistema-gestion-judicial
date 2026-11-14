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
    <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
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

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty districts}">
      <div class="p-5 text-center">
        <i class="bi bi-geo-alt fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">Todavía no hay distritos judiciales registrados.</p>
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
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="district" items="${districts}">
              <tr>
                <td class="cell-strong"><c:out value="${district.code}" /></td>
                <td><c:out value="${district.name}" /></td>
                <td class="cell-muted"><c:out value="${district.seatMunicipality}" /></td>
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
      <div class="px-3 py-3 border-top text-secondary small">
        <c:out value="${districts.size()}" /> distritos judiciales
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
