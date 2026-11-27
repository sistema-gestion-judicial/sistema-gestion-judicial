<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Usuarios" />
<c:set var="activeNav" value="users" />
<c:set var="pageScript" value="status-confirm.js" />
<c:set var="statusAction" value="/users/status" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Usuarios</h1>
    <p class="page-subtitle">
      Oficiales de partes y administradores de <c:out value="${sessionScope.currentUser.courtName}" />.
    </p>
  </div>
  <div class="d-flex flex-column align-items-end gap-3">
    <a class="btn btn-primary" href="${ctx}/users/new">
      <i class="bi bi-person-plus me-1"></i> Registrar usuario
    </a>
  </div>
</header>

<c:if test="${not empty registeredUser}">
  <div class="alert alert-mor-success" role="status">
    <p class="mb-2">
      <i class="bi bi-check-circle me-1"></i>
      El usuario <strong><c:out value="${registeredUser.fullName}" /></strong>
      (<c:out value="${registeredUser.email}" />) quedó registrado como activo.
    </p>
    <p class="mb-2">
      Contraseña temporal: <code class="fs-5 user-select-all"><c:out value="${registeredUser.temporaryPassword}" /></code>
    </p>
    <p class="mb-0 small">
      Entréguela al usuario por un medio seguro. Se muestra una sola vez y el sistema le pedirá cambiarla
      la primera vez que entre.
    </p>
  </div>
</c:if>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>
<c:if test="${not empty flashWarning}">
  <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="alert">
    <i class="bi bi-exclamation-triangle mt-1"></i> <span><c:out value="${flashWarning}" /></span>
  </div>
</c:if>

<form class="filter-bar" method="get" action="${ctx}/users">
  <div class="row g-3 align-items-center">
    <div class="col-lg-5">
      <div class="input-group">
        <span class="input-group-text">
          <i class="bi bi-search"></i>
        </span>
        <input type="search" class="form-control border-start-0" name="q" value="<c:out value='${query}' />"
               placeholder="Buscar por nombre o correo" aria-label="Buscar por nombre o correo">
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
      <a class="link-dark" href="${ctx}/users">Limpiar filtros</a>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-funnel me-1"></i> Aplicar filtros
      </button>
    </div>
  </div>
</form>

<div class="card-mor overflow-hidden">
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Usuarios del juzgado</h2>
      <p class="card-mor-subtitle">Cuentas de los oficiales de partes y administradores.</p>
    </div>
    <p class="card-mor-count"><c:out value="${page.totalItems}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty page.items}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <c:choose>
          <c:when test="${filtered}">
            <p class="empty-state-title">No se encontraron usuarios con esos criterios</p>
            <p class="empty-state-text">Pruebe con otro nombre, correo o estado, o limpie los filtros.</p>
            <a class="btn btn-outline-primary" href="${ctx}/users">Limpiar filtros</a>
          </c:when>
          <c:otherwise>
            <p class="empty-state-title">Todavía no hay usuarios</p>
            <p class="empty-state-text">Registre a los oficiales de partes de este juzgado.</p>
          </c:otherwise>
        </c:choose>
      </div>
    </c:when>
    <c:otherwise>
      <div class="table-responsive">
        <table class="table table-mor">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Correo electrónico</th>
              <th>Teléfono</th>
              <th>Perfil</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="listedUser" items="${page.items}">
              <tr>
                <td class="cell-strong"><c:out value="${listedUser.fullName}" /></td>
                <td class="cell-muted"><c:out value="${listedUser.email}" /></td>
                <td class="cell-muted"><c:out value="${empty listedUser.phone ? '—' : listedUser.phone}" /></td>
                <td class="cell-muted"><c:out value="${listedUser.roleLabel}" /></td>
                <td>
                  <span class="badge-mor ${listedUser.active ? 'status-admissible' : 'status-inadmissible'}">
                    <span class="dot"></span> <c:out value="${listedUser.active ? 'Activo' : 'Inactivo'}" />
                  </span>
                </td>
                <td class="text-end text-nowrap">
                  <c:url var="editUrl" value="/users/edit">
                    <c:param name="email" value="${listedUser.email}" />
                  </c:url>
                  <a class="row-action" href="<c:out value='${editUrl}' />"
                     aria-label="Editar a <c:out value='${listedUser.fullName}' />">
                    <i class="bi bi-pencil" aria-hidden="true"></i> Editar
                  </a>
                  <%-- Administrators cannot deactivate their own account --%>
                  <c:if test="${listedUser.email != sessionScope.currentUser.email}">
                    <button type="button" class="row-action border-0 bg-transparent" data-status-toggle
                            data-code="<c:out value='${listedUser.email}' />"
                            data-name="<c:out value='${listedUser.fullName}' />"
                            data-active="${not listedUser.active}" data-label="la cuenta de"
                            data-title-on="Activar usuario" data-title-off="Desactivar usuario"
                            data-note-on="Podrá volver a entrar al sistema con su contraseña."
                            data-note-off="No podrá entrar al sistema desde su siguiente acción. Las solicitudes que capturó seguirán mostrando su nombre.">
                      <i class="bi ${listedUser.active ? 'bi-toggle-on' : 'bi-toggle-off'}" aria-hidden="true"></i>
                      <c:out value="${listedUser.active ? 'Desactivar' : 'Activar'}" />
                    </button>
                  </c:if>
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
          <nav aria-label="Páginas de usuarios">
            <ul class="pagination mb-0">
              <c:url var="previousUrl" value="/users">
                <c:param name="q" value="${query}" />
                <c:param name="status" value="${selectedStatus}" />
                <c:param name="page" value="${page.number - 1}" />
              </c:url>
              <li class="page-item ${page.first ? 'disabled' : ''}">
                <a class="page-link" href="${page.first ? '#' : previousUrl}" aria-label="Anterior">&laquo;</a>
              </li>
              <c:forEach var="number" begin="1" end="${page.totalPages}">
                <c:url var="numberUrl" value="/users">
                  <c:param name="q" value="${query}" />
                  <c:param name="status" value="${selectedStatus}" />
                  <c:param name="page" value="${number}" />
                </c:url>
                <li class="page-item ${number == page.number ? 'active' : ''}">
                  <a class="page-link" href="${numberUrl}"
                     ${number == page.number ? 'aria-current="page"' : ''}><c:out value="${number}" /></a>
                </li>
              </c:forEach>
              <c:url var="nextUrl" value="/users">
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
