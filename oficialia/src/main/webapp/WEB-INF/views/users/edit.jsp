<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Actualizar usuario" />
<c:set var="activeNav" value="users" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Actualizar usuario</h1>
    <p class="page-subtitle">
      Corrija el nombre, el correo o el perfil. La contraseña solo la cambia su dueño.
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${ctx}/users">Volver a Usuarios</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="section-card" method="post" action="${ctx}/users/edit" novalidate data-unsaved-guard>
      <input type="hidden" name="currentEmail" value="<c:out value='${currentEmail}' />">
      <div class="section-card-header">
        <h2 class="section-card-title">Datos del usuario</h2>
      </div>
      <div class="section-card-body">
        <c:if test="${not empty errors}">
          <div class="alert alert-mor-warning py-2" role="alert">
            <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. Los cambios no se guardaron.
          </div>
        </c:if>

        <div class="row g-3">
          <div class="col-md-6">
            <label for="firstName" class="form-label">Nombre(s) *</label>
            <input type="text" id="firstName" name="firstName" maxlength="${maxNameLength}" autofocus
                   class="form-control ${not empty errors.firstName ? 'is-invalid' : ''}"
                   value="<c:out value='${form.firstName}' />">
            <c:if test="${not empty errors.firstName}">
              <div class="invalid-feedback"><c:out value="${errors.firstName}" /></div>
            </c:if>
          </div>
          <div class="col-md-6">
            <label for="lastName" class="form-label">Apellidos *</label>
            <input type="text" id="lastName" name="lastName" maxlength="${maxNameLength}"
                   class="form-control ${not empty errors.lastName ? 'is-invalid' : ''}"
                   value="<c:out value='${form.lastName}' />">
            <c:if test="${not empty errors.lastName}">
              <div class="invalid-feedback"><c:out value="${errors.lastName}" /></div>
            </c:if>
          </div>
          <div class="col-md-7">
            <label for="email" class="form-label">Correo electrónico *</label>
            <input type="email" id="email" name="email" maxlength="${maxEmailLength}" autocomplete="off"
                   class="form-control ${not empty errors.email ? 'is-invalid' : ''}"
                   value="<c:out value='${form.email}' />">
            <c:if test="${not empty errors.email}">
              <div class="invalid-feedback"><c:out value="${errors.email}" /></div>
            </c:if>
          </div>
          <div class="col-md-5">
            <label for="phone" class="form-label">Teléfono</label>
            <input type="tel" id="phone" name="phone" maxlength="20" autocomplete="off"
                   class="form-control ${not empty errors.phone ? 'is-invalid' : ''}"
                   placeholder="10 dígitos" value="<c:out value='${form.phone}' />">
            <c:if test="${not empty errors.phone}">
              <div class="invalid-feedback"><c:out value="${errors.phone}" /></div>
            </c:if>
          </div>
          <div class="col-md-7">
            <label for="role" class="form-label">Perfil *</label>
            <select id="role" name="role" class="form-select ${not empty errors.role ? 'is-invalid' : ''}">
              <c:forEach var="role" items="${roles}">
                <option value="${role}" ${form.role == role ? 'selected' : ''}><c:out value="${role.label}" /></option>
              </c:forEach>
            </select>
            <c:if test="${not empty errors.role}">
              <div class="invalid-feedback"><c:out value="${errors.role}" /></div>
            </c:if>
          </div>
        </div>
      </div>
      <div class="section-card-footer">
        <a class="btn btn-secondary" href="${ctx}/users">Cancelar</a>
        <button type="submit" class="btn btn-primary">Guardar cambios</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
