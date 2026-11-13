<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Usuarios" />
<c:set var="activeNav" value="users" />
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
    <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
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

<div class="card-mor overflow-hidden">
  <c:choose>
    <c:when test="${empty users}">
      <div class="p-5 text-center">
        <i class="bi bi-people fs-1 text-secondary"></i>
        <p class="mt-2 mb-0">Todavía no hay usuarios registrados en este juzgado.</p>
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
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
      <div class="px-3 py-3 border-top text-secondary small">
        <c:out value="${users.size()}" /> usuarios en el juzgado
      </div>
    </c:otherwise>
  </c:choose>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
