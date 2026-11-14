<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Registrar usuario" />
<c:set var="activeNav" value="users" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <a class="back-link" href="${ctx}/users"><i class="bi bi-arrow-left"></i> Volver a usuarios</a>
    <h1 class="page-title">Registrar usuario</h1>
    <p class="page-subtitle">
      El usuario se registra como Oficial de Partes de <c:out value="${sessionScope.currentUser.courtName}" />.
    </p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="card-mor p-4" method="post" action="${ctx}/users/new" novalidate>
      <c:if test="${not empty errorMessage}">
        <div class="alert alert-mor-warning py-2" role="alert">
          <i class="bi bi-exclamation-triangle me-1"></i> <c:out value="${errorMessage}" />
        </div>
      </c:if>
      <c:if test="${not empty errors}">
        <div class="alert alert-mor-warning py-2" role="alert">
          <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. El usuario no se guardó.
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
                 placeholder="nombre@utez.edu.mx" value="<c:out value='${form.email}' />">
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
      </div>

      <div class="alert alert-mor-info d-flex gap-2 align-items-start mt-4 mb-4" role="note">
        <i class="bi bi-info-circle mt-1"></i>
        <span>El sistema genera una contraseña temporal y la muestra una sola vez al guardar. La primera vez que
          el usuario entre, deberá cambiarla.</span>
      </div>

      <div class="d-flex justify-content-end gap-3">
        <a class="btn btn-outline-primary" href="${ctx}/users">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="bi bi-person-plus me-1"></i> Registrar usuario</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
