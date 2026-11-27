<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Usuarios" />
<c:set var="activeNav" value="users" />
<c:set var="pageScript" value="user-status.js" />
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

<div class="card-mor overflow-hidden">
  <div class="card-mor-header">
    <div>
      <h2 class="card-mor-title">Usuarios del juzgado</h2>
      <p class="card-mor-subtitle">Cuentas de los oficiales de partes y administradores.</p>
    </div>
    <p class="card-mor-count"><c:out value="${users.size()}" /> registros</p>
  </div>
  <c:choose>
    <c:when test="${empty users}">
      <div class="empty-state">
        <span class="empty-state-icon" aria-hidden="true"></span>
        <p class="empty-state-title">Todavía no hay usuarios</p>
        <p class="empty-state-text">Registre a los oficiales de partes de este juzgado.</p>
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
            <c:forEach var="listedUser" items="${users}">
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
                    <c:param name="id" value="${listedUser.id}" />
                  </c:url>
                  <a class="row-action" href="${editUrl}" aria-label="Editar a <c:out value='${listedUser.fullName}' />">
                    <i class="bi bi-pencil" aria-hidden="true"></i> Editar
                  </a>
                  <%-- Opens the confirmation that names the user; the change is posted from there --%>
                  <button type="button" class="row-action" data-bs-toggle="modal" data-bs-target="#statusModal"
                          data-user-id="${listedUser.id}" data-user-name="<c:out value='${listedUser.fullName}' />"
                          data-next-status="${listedUser.active ? 'inactive' : 'active'}">
                    <c:choose>
                      <c:when test="${listedUser.active}">
                        <i class="bi bi-person-dash" aria-hidden="true"></i> Desactivar
                      </c:when>
                      <c:otherwise>
                        <i class="bi bi-person-check" aria-hidden="true"></i> Activar
                      </c:otherwise>
                    </c:choose>
                  </button>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <div class="card-mor-footer">
        Mostrando <c:out value="${users.size()}" /> de <c:out value="${users.size()}" />
      </div>
    </c:otherwise>
  </c:choose>
</div>

<div class="modal modal-mor fade" id="statusModal" tabindex="-1" aria-labelledby="statusModalTitle">
  <div class="modal-dialog modal-dialog-centered">
    <form class="modal-content" method="post" action="${ctx}/users/status">
      <input type="hidden" name="id" data-status-id>
      <input type="hidden" name="status" data-status-value>
      <div class="modal-header">
        <h2 class="modal-title" id="statusModalTitle" data-status-title>Cambiar el estado de la cuenta</h2>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
      </div>
      <div class="modal-body">
        <p class="modal-lead" data-status-question></p>
        <p class="mb-3" data-status-effect></p>
        <label for="statusReason" class="form-label">Motivo del cambio</label>
        <textarea id="statusReason" name="reason" class="form-control" rows="3" maxlength="${maxReasonLength}"
                  placeholder="Opcional. Queda registrado en la bitácora."></textarea>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-primary" data-status-submit>Confirmar</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
